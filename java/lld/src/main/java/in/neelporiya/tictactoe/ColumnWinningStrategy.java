package in.neelporiya.tictactoe;

/**
 * Checks whether the last move completed its column.
 */
public final class ColumnWinningStrategy implements WinningStrategy {

    @Override
    public boolean isWinningMove(Board board, Move move) {
        for (int row = 0; row < board.getSize(); row++) {
            if (board.getPieceAt(row, move.column()) != move.piece()) {
                return false;
            }
        }
        return true;
    }
}
