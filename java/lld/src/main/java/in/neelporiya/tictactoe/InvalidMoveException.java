package in.neelporiya.tictactoe;

/**
 * Raised when a move violates board bounds, occupancy, turn order, or terminal-game rules.
 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
