package in.neelporiya.snakeandladder;

import java.util.Objects;

/**
 * Player identity plus current board position.
 */
public final class Player {

    private final String id;
    private final String name;
    private volatile int position;

    public Player(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    void moveTo(int position) {
        this.position = position;
    }

    void reset() {
        this.position = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
}
