package in.neelporiya.chess;

import java.util.Objects;

public final class Player {

    private final String name;
    private final Color color;

    public Player(String name, Color color) {
        this.name = Objects.requireNonNull(name, "name");
        this.color = Objects.requireNonNull(color, "color");
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Player player)) {
            return false;
        }
        return name.equals(player.name) && color == player.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return name + " (" + color + ")";
    }
}
