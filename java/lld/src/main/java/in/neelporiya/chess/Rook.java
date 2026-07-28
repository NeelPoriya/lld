package in.neelporiya.chess;

import java.util.Set;

public final class Rook extends Piece {

    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    public Rook(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        return collectLineMoves(board, from, DIRECTIONS);
    }
}
