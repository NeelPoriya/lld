package in.neelporiya.hotelmanagement.model;

import java.util.Objects;

public final class Room {

    private final String id;
    private final String number;
    private final RoomType type;
    private final int floor;

    public Room(String id, String number, RoomType type, int floor) {
        this.id = requireText(id, "id");
        this.number = requireText(number, "number");
        this.type = Objects.requireNonNull(type, "type");
        this.floor = floor;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    public int getFloor() {
        return floor;
    }

    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
