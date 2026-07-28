package in.neelporiya.tictactoe;

import java.util.Objects;

/**
 * Immutable value object representing a single attempted placement.
 */
public record Move(Player player, int row, int column, Piece piece) {

    public Move(Player player, int row, int column) {
        this(player, row, column, Objects.requireNonNull(player, "player").getPiece());
    }

    public Move {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(piece, "piece");
    }
}
