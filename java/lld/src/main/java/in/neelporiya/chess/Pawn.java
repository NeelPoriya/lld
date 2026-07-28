package in.neelporiya.chess;

import java.util.HashSet;
import java.util.Set;

public final class Pawn extends Piece {

    public Pawn(Color color) {
        super(color);
    }

    @Override
    public Set<Position> legalMoves(Board board, Position from) {
        Set<Position> moves = new HashSet<>();
        int direction = getColor() == Color.WHITE ? -1 : 1;
        int startRow = getColor() == Color.WHITE ? 6 : 1;
        int oneStepRow = from.row() + direction;

        if (Position.isInside(oneStepRow, from.column()) && board.isEmpty(new Position(oneStepRow, from.column()))) {
            moves.add(new Position(oneStepRow, from.column()));
            int twoStepRow = from.row() + 2 * direction;
            if (from.row() == startRow
                    && Position.isInside(twoStepRow, from.column())
                    && board.isEmpty(new Position(twoStepRow, from.column()))) {
                moves.add(new Position(twoStepRow, from.column()));
            }
        }

        addCaptureIfEnemy(board, moves, oneStepRow, from.column() - 1);
        addCaptureIfEnemy(board, moves, oneStepRow, from.column() + 1);
        return moves;
    }

    private void addCaptureIfEnemy(Board board, Set<Position> moves, int row, int column) {
        if (!Position.isInside(row, column)) {
            return;
        }
        Position candidate = new Position(row, column);
        Piece occupant = board.pieceAt(candidate);
        if (occupant != null && occupant.getColor() != getColor()) {
            moves.add(candidate);
        }
    }
}
