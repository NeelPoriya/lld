package in.neelporiya.hotelmanagement.repository;

import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.ReservationStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ReservationRepository {

    private final ConcurrentMap<String, Reservation> reservationsById = new ConcurrentHashMap<>();

    public void save(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        reservationsById.put(reservation.getId(), reservation);
    }

    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(reservationsById.get(id));
    }

    public List<Reservation> activeForRoom(String roomId) {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getRoomId().equals(roomId))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED
                        || reservation.getStatus() == ReservationStatus.CHECKED_IN)
                .toList();
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservationsById.values());
    }
}
