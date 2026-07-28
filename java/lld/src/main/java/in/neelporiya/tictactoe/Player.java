package in.neelporiya.tictactoe;

import java.util.Objects;

/**
 * A human or bot participant. Identity is intentionally tiny for interview focus.
 */
public final class Player {

    private final String name;
    private final Piece piece;

    public Player(String name, Piece piece) {
        this.name = Objects.requireNonNull(name, "name");
        this.piece = Objects.requireNonNull(piece, "piece");
    }

    public String getName() {
        return name;
    }

    public Piece getPiece() {
        return piece;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Player player)) {
            return false;
        }
        return name.equals(player.name) && piece == player.piece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, piece);
    }

    @Override
    public String toString() {
        return name + "(" + piece + ")";
    }
}
