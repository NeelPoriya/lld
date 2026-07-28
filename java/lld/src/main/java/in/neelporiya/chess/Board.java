package in.neelporiya.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Board {

    public static final int SIZE = 8;

    private final Cell[][] cells = new Cell[SIZE][SIZE];

    public Board() {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                cells[row][column] = new Cell(new Position(row, column));
            }
        }
    }

    public Cell getCell(Position position) {
        Objects.requireNonNull(position, "position");
        return cells[position.row()][position.column()];
    }

    public Optional<Piece> getPiece(Position position) {
        return getCell(position).getPiece();
    }

    Piece pieceAt(Position position) {
        return getCell(position).pieceOrNull();
    }

    public boolean isEmpty(Position position) {
        return getCell(position).isEmpty();
    }

    public void clear() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                cell.setPiece(null);
            }
        }
    }

    public void placePiece(Position position, Piece piece) {
        getCell(position).setPiece(Objects.requireNonNull(piece, "piece"));
    }

    public Piece removePiece(Position position) {
        Cell cell = getCell(position);
        Piece removed = cell.pieceOrNull();
        cell.setPiece(null);
        return removed;
    }

    Move movePiece(Position from, Position to) {
        Piece moving = pieceAt(from);
        if (moving == null) {
            throw new InvalidMoveException("no piece at " + from);
        }
        Piece captured = pieceAt(to);
        getCell(to).setPiece(moving);
        getCell(from).setPiece(null);
        return new Move(from, to, moving, captured);
    }

    void undoMove(Move move) {
        getCell(move.from()).setPiece(move.movedPiece());
        getCell(move.to()).setPiece(move.capturedPiece());
    }

    public List<Position> positionsOccupiedBy(Color color) {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                Position position = new Position(row, column);
                Piece piece = pieceAt(position);
                if (piece != null && piece.getColor() == color) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }
}
