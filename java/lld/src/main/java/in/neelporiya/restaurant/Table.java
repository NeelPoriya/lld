package in.neelporiya.restaurant;

import java.util.Objects;

/** A physical restaurant table with a seating capacity. */
public class Table {
    private final String id;
    private final int capacity;

    public Table(String id, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }
}
