package in.neelporiya.chess;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * // DESIGN PATTERN: Strategy / polymorphism — every Piece subtype owns its movement rule.
 */
public abstract class Piece {

    private final Color color;

    protected Piece(Color color) {
        this.color = Objects.requireNonNull(color, "color");
    }

    public Color getColor() {
        return color;
    }

    public abstract Set<Position> legalMoves(Board board, Position from);

    protected Set<Position> collectLineMoves(Board board, Position from, int[][] directions) {
        Set<Position> moves = new HashSet<>();
        for (int[] direction : directions) {
            int row = from.row() + direction[0];
            int column = from.column() + direction[1];
            while (Position.isInside(row, column)) {
                Position candidate = new Position(row, column);
                Piece occupant = board.pieceAt(candidate);
                if (occupant == null) {
                    moves.add(candidate);
                } else {
                    if (occupant.getColor() != color) {
                        moves.add(candidate);
                    }
                    break;
                }
                row += direction[0];
                column += direction[1];
            }
        }
        return moves;
    }

    protected void addIfAvailable(Board board, Set<Position> moves, int row, int column) {
        if (!Position.isInside(row, column)) {
            return;
        }
        Position candidate = new Position(row, column);
        Piece occupant = board.pieceAt(candidate);
        if (occupant == null || occupant.getColor() != color) {
            moves.add(candidate);
        }
    }
}
