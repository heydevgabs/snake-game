package websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.GameStateDTO;
import dtos.PointerDTO;
import dtos.SnakeDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import models.Player;
import models.Pointer;
import models.Snake;
import services.GameService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * WebSocket server endpoint that manages communication between the server and connected game clients.
 * Handles the game logic such as player connections, movements, and broadcasting the game state to all players.
 */
@ApplicationScoped
@ServerEndpoint( "/game" )
@Slf4j
public class GameWebSocket {

    /**
     * A thread-safe map that holds the active WebSocket sessions and their associated Player objects.
     * Key: WebSocket session (used for communication with the client).
     * Value: Player object that holds the player's information and their controlled snake.
     */
    private static final Map< Session, Player > players = new ConcurrentHashMap<>( );

    @Inject
    GameService gameService;

    /**
     * Handles the event when a new WebSocket connection is opened.
     * Initializes a new player and their snake, and adds them to the game state.
     *
     * @param session The WebSocket session representing the connection to the player.
     */
    @OnOpen
    public void onOpen( Session session ) {
        Player player = new Player( session.getId( ), "Player" + session.getId( ), new Snake( new Pointer( 5, 5 ) ) );
        players.put( session, player );
        gameService.addPlayer( session.getId( ), player );
    }

    /**
     * Handles incoming messages from a client (e.g., player movements).
     * The message is expected to contain the direction the player wants their snake to move.
     *
     * @param message The incoming message from the client, expected to be in JSON format.
     * @param session The WebSocket session from which the message was received.
     */
    @OnMessage
    public void onMessage( String message, Session session ) {
        Map< String, Object > request = parseMessage( message );
        String direction = ( String ) request.get( "direction" );

        gameService.movePlayer( session.getId( ), direction );
    }

    /**
     * Retrieves the WebSocket session associated with a specific player ID.
     *
     * @param playerId The unique identifier of the player (WebSocket session ID).
     * @return An Optional containing the player's WebSocket session if found, or empty if not found.
     */
    public Optional< Session > getSessionByPlayerId( String playerId ) {
        return players.entrySet( ).stream( )
                .filter( entry -> entry.getValue( ).getId( ).equals( playerId ) )
                .map( Map.Entry::getKey )
                .findFirst( );
    }

    /**
     * Handles the event when a WebSocket connection is closed (i.e., a player disconnects).
     * Removes the player and their snake from the game.
     *
     * @param session The WebSocket session that was closed.
     */
    @OnClose
    public void onClose( Session session ) {
        gameService.removePlayer( session.getId( ) );
        players.remove( session );
    }

    /**
     * Broadcasts the current game state (i.e., all snakes and the food position) to all connected clients.
     * Converts the game state to a DTO and sends it as a JSON object.
     */
    public void broadcastGameState( ) {
        Map< String, SnakeDTO > snakeDTOs = new HashMap<>( );

        for ( Map.Entry< String, Snake > entry : gameService.getGameState( ).getSnakes( ).entrySet( ) ) {
            LinkedList< PointerDTO > bodyDTOs = new LinkedList<>( );

            for ( Pointer p : entry.getValue( ).getBody( ) ) {
                bodyDTOs.add( new PointerDTO( p.getX( ), p.getY( ) ) );
            }

            snakeDTOs.put( entry.getKey( ), new SnakeDTO( bodyDTOs, entry.getValue( ).getDirection( ).toString( ), entry.getValue( ).isAlive( ) ) );
        }

        GameStateDTO gameStateDTO = new GameStateDTO(
                snakeDTOs,
                new PointerDTO(
                        gameService.getGameState( ).getFood( ).getPosition( ).getX( ),
                        gameService.getGameState( ).getFood( ).getPosition( ).getY( )
                )
        );

        try {
            String gameStateJson = new ObjectMapper( ).writeValueAsString( gameStateDTO );

            for ( Session session : players.keySet( ) ) {
                session.getAsyncRemote( ).sendText( gameStateJson, result -> {
                    if ( result.getException( ) != null ) {
                        log.error( "Error sending game state to player {}: {}", session.getId( ), result.getException( ).getMessage( ) );
                    } else {
                        log.info( "Game state sent to player: {}", session.getId( ) );
                    }
                } );
            }
        } catch (Exception e) {
            log.error( "Error serializing GameStateDTO: {}", e.getMessage( ) );
        }
    }


    /**
     * Helper function to parse an incoming message (JSON format) into a Map.
     *
     * @param message The incoming JSON message as a string.
     * @return A map containing the parsed data from the message.
     */
    private Map< String, Object > parseMessage( String message ) {
        try {
            return new ObjectMapper( ).readValue( message, new TypeReference<>( ) {
            } );
        } catch (Exception e) {
            log.error( "Error parsing message: {}", e.getMessage( ) );
        }
        return new HashMap<>( );
    }
}