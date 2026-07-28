package in.neelporiya.tictactoe;

/**
 * Checks both diagonals, but only when the last move lies on the relevant diagonal.
 */
public final class DiagonalWinningStrategy implements WinningStrategy {

    @Override
    public boolean isWinningMove(Board board, Move move) {
        return completesPrimaryDiagonal(board, move) || completesSecondaryDiagonal(board, move);
    }

    private boolean completesPrimaryDiagonal(Board board, Move move) {
        if (move.row() != move.column()) {
            return false;
        }
        for (int index = 0; index < board.getSize(); index++) {
            if (board.getPieceAt(index, index) != move.piece()) {
                return false;
            }
        }
        return true;
    }

    private boolean completesSecondaryDiagonal(Board board, Move move) {
        int last = board.getSize() - 1;
        if (move.row() + move.column() != last) {
            return false;
        }
        for (int row = 0; row < board.getSize(); row++) {
            if (board.getPieceAt(row, last - row) != move.piece()) {
                return false;
            }
        }
        return true;
    }
}
