package dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

/**
 * DTO representing a snake in the game.
 * Contains the snake's body segments, direction, and whether it is alive.
 */
@Getter
@Setter
public class SnakeDTO {
    private LinkedList<PointerDTO> body;
    private String direction;
    private boolean isAlive;

    /**
     * Constructs a SnakeDTO with the given body, direction, and alive status.
     *
     * @param body the list of points representing the snake's body
     * @param direction the current direction of the snake
     * @param isAlive whether the snake is alive
     */
    public SnakeDTO(LinkedList<PointerDTO> body, String direction, boolean isAlive) {
        this.body = body;
        this.direction = direction;
        this.isAlive = isAlive;
    }
}