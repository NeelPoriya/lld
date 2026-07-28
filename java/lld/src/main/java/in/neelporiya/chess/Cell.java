package in.neelporiya.chess;

import java.util.Optional;

public final class Cell {

    private final Position position;
    private Piece piece;

    Cell(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Optional<Piece> getPiece() {
        return Optional.ofNullable(piece);
    }

    boolean isEmpty() {
        return piece == null;
    }

    Piece pieceOrNull() {
        return piece;
    }

    void setPiece(Piece piece) {
        this.piece = piece;
    }
}
