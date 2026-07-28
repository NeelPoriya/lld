package in.neelporiya.restaurant;

import java.time.Instant;
import java.util.Objects;

/** Reservation for one table and one time slot. */
public class Reservation {
    private final String id;
    private final Table table;
    private final String guestName;
    private final TimeSlot slot;
    private final Instant createdAt;

    public Reservation(String id, Table table, String guestName, TimeSlot slot, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.table = Objects.requireNonNull(table, "table");
        this.guestName = Objects.requireNonNull(guestName, "guestName");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public String getId() {
        return id;
    }

    public Table getTable() {
        return table;
    }

    public String getGuestName() {
        return guestName;
    }

    public TimeSlot getSlot() {
        return slot;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
