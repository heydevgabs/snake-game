package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import models.*;
import websocket.GameWebSocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class that handles the core game logic, such as moving snakes,
 * adding/removing players, detecting collisions, and broadcasting the game state.
 */
@Getter
@ApplicationScoped
@Slf4j
public class GameService {
    private final GameState gameState;

    @Inject
    GameWebSocket gameWebSocket;

    /**
     * Constructs a GameService with the given WebSocket endpoint for communication.
     * Initializes the game state with a grid size of 40x40.
     *
     * @param gameWebSocket the WebSocket endpoint for broadcasting game state
     */
    @Inject
    public GameService( GameWebSocket gameWebSocket ) {
        if ( gameWebSocket == null ) {
            throw new IllegalArgumentException( "GameWebSocket must be provided!" );
        }
        this.gameWebSocket = gameWebSocket;
        this.gameState = new GameState( 40, 40 );
    }

    /**
     * Adds a new player and their snake to the game.
     *
     * @param playerId the player's ID
     * @param player   the Player object containing the player's details and snake
     */
    public void addPlayer( String playerId, Player player ) {
        Snake snake = new Snake( new Pointer( 5, 5 ) );  // Start position for snake
        player.setSnake( snake );
        gameState.addSnake( playerId, snake );
        log.info( "Player {} joined with a new snake at: {}", playerId, snake.getBody( ).getFirst( ) );
    }

    /**
     * Removes a player and their snake from the game.
     *
     * @param playerId the player's ID
     */
    public void removePlayer( String playerId ) {
        gameState.removeSnake( playerId );
    }

    /**
     * Moves the player's snake based on the given direction and checks for collisions.
     *
     * @param playerId  the player's ID
     * @param direction the direction the player wants to move their snake
     */
    public void movePlayer( String playerId, String direction ) {
        if ( direction == null ) {
            return;
        }
        try {
            Snake snake = gameState.getSnakes( ).get( playerId );
            if ( snake != null && snake.isAlive( ) ) {
                snake.setDirection( Direction.valueOf( direction.toUpperCase( ) ) );
                snake.move( );
                checkCollisions( playerId, snake );

                gameWebSocket.broadcastGameState( );
            }
        } catch (IllegalArgumentException e) {
            log.error( "Invalid direction '{}' for player {}", direction, playerId );
        }
    }

    /**
     * Checks for collisions and notifies the client if a collision or food-eating event occurs.
     *
     * @param playerId the player's ID
     * @param snake    the snake to check for collisions
     */
    private void checkCollisions( String playerId, Snake snake ) {
        Pointer head = snake.getHead( );
        boolean collisionOccurred = false;

        if ( head.getX( ) < 0 || head.getX( ) >= gameState.getGridWidth( ) || head.getY( ) < 0 || head.getY( ) >= gameState.getGridHeight( ) ) {
            snake.setAlive( false );
            gameState.removeSnake( playerId );
            collisionOccurred = true;
            log.warn( "Player {}'s snake hit the wall and is removed.", playerId );
        }

        if ( !collisionOccurred ) {
            for ( int i = 1; i < snake.getBody( ).size( ); i++ ) {
                Pointer bodyPart = snake.getBody( ).get( i );
                if ( head.equals( bodyPart ) ) {
                    snake.setAlive( false );
                    gameState.removeSnake( playerId );
                    collisionOccurred = true;
                    log.warn( "Player {}'s snake collided with itself and is removed.", playerId );
                    break;  // Exit loop early on collision
                }
            }
        }

        for ( Map.Entry< String, Snake > entry : gameState.getSnakes( ).entrySet( ) ) {
            if ( !entry.getKey( ).equals( playerId ) ) {
                for ( Pointer segment : entry.getValue( ).getBody( ) ) {
                    if ( head.equals( segment ) ) {
                        snake.setAlive( false );
                        gameState.removeSnake( playerId );
                        collisionOccurred = true;
                        log.warn( "Player {}'s snake collided with another snake and is removed.", playerId );
                        break;
                    }
                }
            }
        }

        if ( collisionOccurred ) {
            Map< String, Object > collisionResponse = new HashMap<>( );
            collisionResponse.put( "collision", true );
            collisionResponse.put( "playerId", playerId );
            gameWebSocket.broadcastGameState( );
            broadcastMessageToClient( playerId, collisionResponse );
        }

        if ( head.equals( gameState.getFood( ).getPosition( ) ) ) {
            snake.grow( );
            gameState.generateNewFood( );
            log.info( "Player {}'s snake ate the food and grew.", playerId );

            Map< String, Object > foodEatenResponse = new HashMap<>( );
            foodEatenResponse.put( "foodEaten", true );
            foodEatenResponse.put( "playerId", playerId );
            broadcastMessageToClient( playerId, foodEatenResponse );
        }
    }

    /**
     * Sends a message to the client identified by the player ID.
     *
     * @param playerId the player's ID
     * @param message  the message to send
     */
    private void broadcastMessageToClient( String playerId, Map< String, Object > message ) {
        try {
            gameWebSocket.getSessionByPlayerId( playerId ).ifPresent( session -> {
                try {
                    session.getAsyncRemote( ).sendText( new ObjectMapper( ).writeValueAsString( message ) );
                } catch (Exception e) {
                    log.error( "Error sending message to player {}: {}", playerId, e.getMessage( ) );
                }
            } );
        } catch (Exception e) {
            log.error( "Error processing message for player {}: {}", playerId, e.getMessage( ) );
        }
    }
}