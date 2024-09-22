package dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * DTO representing the current state of the game.
 * This object is sent from the server to the client, containing all snake positions and the food position.
 */
@Getter
@Setter
public class GameStateDTO {
    private Map<String, SnakeDTO> snakes;
    private PointerDTO foodPosition;

    /**
     * Constructs a GameStateDTO with the given snake positions and food position.
     *
     * @param snakes a map of player IDs to their respective SnakeDTO objects
     * @param foodPosition the current position of the food
     */
    public GameStateDTO(Map<String, SnakeDTO> snakes, PointerDTO foodPosition) {
        this.snakes = snakes;
        this.foodPosition = foodPosition;
    }
}