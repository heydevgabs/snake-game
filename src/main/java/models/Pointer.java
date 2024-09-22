package models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a point on the grid, used for snake segments or food positions.
 */
@Data
@AllArgsConstructor
public class Pointer {
    private int x;
    private int y;
}