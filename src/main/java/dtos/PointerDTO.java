package dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing a point on the game grid.
 * Used for snake body segments or food positions.
 */
@Getter
@Setter
@AllArgsConstructor
public class PointerDTO {
    private int x;
    private int y;
}