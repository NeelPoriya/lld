package in.neelporiya.tictactoe;

/**
 * Checks whether the last move completed its row.
 */
public final class RowWinningStrategy implements WinningStrategy {

    @Override
    public boolean isWinningMove(Board board, Move move) {
        for (int column = 0; column < board.getSize(); column++) {
            if (board.getPieceAt(move.row(), column) != move.piece()) {
                return false;
            }
        }
        return true;
    }
}
