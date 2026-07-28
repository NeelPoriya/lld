package in.neelporiya.hotelmanagement.repository;

import in.neelporiya.hotelmanagement.model.Room;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RoomRepository {

    private final ConcurrentMap<String, Room> roomsById = new ConcurrentHashMap<>();

    public void save(Room room) {
        Objects.requireNonNull(room, "room");
        roomsById.put(room.getId(), room);
    }

    public Optional<Room> findById(String id) {
        return Optional.ofNullable(roomsById.get(id));
    }

    public List<Room> findAll() {
        return List.copyOf(roomsById.values());
    }
}
