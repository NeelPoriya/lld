package in.neelporiya.chess;

import java.util.HashSet;
import java.util.Set;

public final class Knight extends Piece {

    private static final int[][] OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public Knight(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        Set<Position> moves = new HashSet<>();
        for (int[] offset : OFFSETS) {
            addIfAvailable(board, moves, from.row() + offset[0], from.column() + offset[1]);
        }
        return moves;
    }
}
