package in.neelporiya.hotelmanagement.model;

public final class RoomFactory {

    private RoomFactory() {
    }

    public static Room create(String id, String number, RoomType type, int floor) {
        // DESIGN PATTERN: Factory centralizes room construction so later room metadata stays in one place.
        return new Room(id, number, type, floor);
    }
}
