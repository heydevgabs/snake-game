package models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Represents the food object in the game.
 * The food is placed at a random position on the game grid.
 */
@Getter
@Setter
@Slf4j
public class Food {
    private Pointer position;

    /**
     * Initializes the food at a random position within the given grid size.
     *
     * @param gridWidth the width of the game grid
     * @param gridHeight the height of the game grid
     */
    public Food(int gridWidth, int gridHeight) {
        this.position = generateRandomPosition(gridWidth, gridHeight);
        log.info("Food generated at position: {}", this.position);
    }

    /**
     * Generates a random position on the grid for the food.
     *
     * @param width the width of the grid
     * @param height the height of the grid
     * @return the random position for the food
     */
    public Pointer generateRandomPosition(int width, int height) {
        Random random = new Random();
        return new Pointer(random.nextInt(width), random.nextInt(height));
    }
}