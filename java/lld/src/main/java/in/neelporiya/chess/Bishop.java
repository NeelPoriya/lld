package in.neelporiya.chess;

import java.util.Set;

public final class Bishop extends Piece {

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        return collectLineMoves(board, from, DIRECTIONS);
    }
}
