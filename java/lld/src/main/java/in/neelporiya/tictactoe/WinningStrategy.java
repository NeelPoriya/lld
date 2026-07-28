package in.neelporiya.tictactoe;

/**
 * // DESIGN PATTERN: Strategy — Game does not know what a winning line means.
 */
public interface WinningStrategy {

    boolean isWinningMove(Board board, Move move);
}
