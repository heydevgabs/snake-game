package models;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a player in the game.
 * Each player controls a snake.
 */
@Getter
@Setter
public class Player {
    private String id;
    private String name;
    private Snake snake;

    /**
     * Constructs a player with the given ID, name, and snake.
     *
     * @param id    the unique ID for the player
     * @param name  the name of the player
     * @param snake the snake controlled by the player
     */
    public Player( String id, String name, Snake snake ) {
        this.id = id;
        this.name = name;
        this.snake = snake;
    }
}