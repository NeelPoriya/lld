package in.neelporiya.chess;

import java.util.Set;

public final class Queen extends Piece {

    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    public Queen(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        return collectLineMoves(board, from, DIRECTIONS);
    }
}
