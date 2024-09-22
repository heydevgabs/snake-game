package models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the current state of the game.
 * Manages the grid size, snakes, and food.
 */
@Getter
@Setter
@Slf4j
public class GameState {
    private int gridWidth;
    private int gridHeight;
    private Map< String, Snake > snakes = new HashMap<>( );
    private Food food;

    /**
     * Initializes the game state with the given grid size and generates the first food item.
     *
     * @param gridWidth  the width of the game grid
     * @param gridHeight the height of the game grid
     */
    public GameState( int gridWidth, int gridHeight ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        generateNewFood( );  // Generate initial food
        log.info( "GameState initialized with grid size: {}x{}", gridWidth, gridHeight );
    }

    /**
     * Adds a snake to the game for the given player.
     *
     * @param playerId the ID of the player
     * @param snake    the snake object to add
     */
    public void addSnake( String playerId, Snake snake ) {
        snakes.put( playerId, snake );
        log.info( "Snake added for player: {}", playerId );
    }

    /**
     * Removes a snake from the game for the given player.
     *
     * @param playerId the ID of the player
     */
    public void removeSnake( String playerId ) {
        snakes.remove( playerId );
        log.info( "Snake removed for player: {}", playerId );
    }

    /**
     * Generates a new food object at a random position within the grid.
     */
    public void generateNewFood( ) {
        this.food = new Food( gridWidth, gridHeight );
        log.info( "Generated new food at position: {}", food.getPosition( ) );
    }
}