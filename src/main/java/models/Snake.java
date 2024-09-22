package models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * Represents a snake in the game.
 * The snake can move, grow, and check for collisions.
 */
@Getter
@Setter
@Slf4j
public class Snake {
    private LinkedList< Pointer > body;
    private Direction direction;
    private boolean alive = true;

    /**
     * Constructs a new snake at the given initial position.
     * The snake starts with a single segment and is set to move to the RIGHT by default.
     *
     * @param initialPosition the initial position of the snake's head
     */
    public Snake( Pointer initialPosition ) {
        body = new LinkedList<>( );
        body.add( initialPosition );
        this.direction = Direction.RIGHT;
        log.info( "Created a new snake at position: {}", initialPosition );
    }

    /**
     * Returns the head of the snake, which is the first segment of its body.
     *
     * @return the head of the snake
     */
    public Pointer getHead( ) {
        return this.body.getFirst( );
    }

    /**
     * Moves the snake in its current direction.
     * Adds a new segment at the head and removes the tail, simulating movement.
     */
    public void move( ) {
        if ( !alive ) {
            log.info( "Snake is dead. Cannot move." );
            return;  // Snake cannot move if it is dead
        }

        Pointer head = body.getFirst( );
        Pointer newHead = switch (direction) {
            case UP -> new Pointer( head.getX( ), head.getY( ) - 1 );
            case DOWN -> new Pointer( head.getX( ), head.getY( ) + 1 );
            case LEFT -> new Pointer( head.getX( ) - 1, head.getY( ) );
            case RIGHT -> new Pointer( head.getX( ) + 1, head.getY( ) );
        };
        body.addFirst( newHead );
        body.removeLast( );
        log.info( "Snake moved to: {}", newHead );
    }

    /**
     * Grows the snake by adding a new segment at the tail.
     */
    public void grow( ) {
        body.addLast( body.getLast( ) );
        log.info( "Snake grew. New length: {}", body.size( ) );
    }
}