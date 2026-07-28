package in.neelporiya.chess;

/**
 * // DESIGN PATTERN: Factory centralizes standard chess setup and keeps Game construction clean.
 */
public final class BoardFactory {

    private BoardFactory() {
    }

    public static Board standardBoard() {
        Board board = emptyBoard();
        setupBackRank(board, Color.BLACK, 0);
        setupPawns(board, Color.BLACK, 1);
        setupPawns(board, Color.WHITE, 6);
        setupBackRank(board, Color.WHITE, 7);
        return board;
    }

    public static Board emptyBoard() {
        return new Board();
    }

    private static void setupPawns(Board board, Color color, int row) {
        for (int column = 0; column < Board.SIZE; column++) {
            board.placePiece(new Position(row, column), new Pawn(color));
        }
    }

    private static void setupBackRank(Board board, Color color, int row) {
        board.placePiece(new Position(row, 0), new Rook(color));
        board.placePiece(new Position(row, 1), new Knight(color));
        board.placePiece(new Position(row, 2), new Bishop(color));
        board.placePiece(new Position(row, 3), new Queen(color));
        board.placePiece(new Position(row, 4), new King(color));
        board.placePiece(new Position(row, 5), new Bishop(color));
        board.placePiece(new Position(row, 6), new Knight(color));
        board.placePiece(new Position(row, 7), new Rook(color));
    }
}
