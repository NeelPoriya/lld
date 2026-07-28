package in.neelporiya.carrental.repository;

import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;

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

    public List<Reservation> activeForVehicle(String vehicleId) {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getVehicleId().equals(vehicleId))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.RESERVED
                        || reservation.getStatus() == ReservationStatus.ONGOING)
                .toList();
    }

    public List<Reservation> findAll() {
        return List.copyOf(reservationsById.values());
    }
}
