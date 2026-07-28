package in.neelporiya.chess;

import java.util.HashSet;
import java.util.Set;

public final class King extends Piece {

    public King(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        Set<Position> moves = new HashSet<>();
        for (int rowDelta = -1; rowDelta <= 1; rowDelta++) {
            for (int columnDelta = -1; columnDelta <= 1; columnDelta++) {
                if (rowDelta != 0 || columnDelta != 0) {
                    addIfAvailable(board, moves, from.row() + rowDelta, from.column() + columnDelta);
                }
            }
        }
        return moves;
    }
}
