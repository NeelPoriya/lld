package in.neelporiya.hotelmanagement.search;

import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.RoomType;
import in.neelporiya.hotelmanagement.model.StayRange;
import in.neelporiya.hotelmanagement.repository.ReservationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultRoomSearchStrategy implements SearchStrategy {

    @Override
    public List<Room> search(List<Room> rooms, ReservationRepository reservations, StayRange range, Optional<RoomType> type) {
        Objects.requireNonNull(rooms, "rooms");
        Objects.requireNonNull(reservations, "reservations");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(type, "type");
        // DESIGN PATTERN: Strategy lets the interview evolve from "first room" to "cheapest" or "best view" search.
        return rooms.stream()
                .filter(room -> type.map(roomType -> room.getType() == roomType).orElse(true))
                .filter(room -> reservations.activeForRoom(room.getId()).stream()
                        .noneMatch(reservation -> reservation.getRange().overlaps(range)))
                .sorted(Comparator.comparingInt(Room::getFloor).thenComparing(Room::getNumber))
                .toList();
    }
}
