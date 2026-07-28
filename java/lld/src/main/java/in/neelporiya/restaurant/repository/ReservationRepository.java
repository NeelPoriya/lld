package in.neelporiya.restaurant.repository;

import in.neelporiya.restaurant.Reservation;
import in.neelporiya.restaurant.TimeSlot;
import in.neelporiya.restaurant.exception.ReservationConflictException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Repository with per-table locking for atomic reservation conflict checks. */
public class ReservationRepository {
    private final Map<String, List<Reservation>> reservationsByTable = new ConcurrentHashMap<>();
    private final Map<String, Object> locksByTable = new ConcurrentHashMap<>();

    public void saveIfAvailable(Reservation reservation) {
        String tableId = reservation.getTable().getId();
        Object lock = locksByTable.computeIfAbsent(tableId, ignored -> new Object());
        synchronized (lock) {
            // CONCURRENCY: overlap-check + insert happen under the same per-table lock, so two threads
            // cannot both observe an empty slot list and double-book the same table/time interval.
            List<Reservation> existing = reservationsByTable.computeIfAbsent(tableId, ignored -> new ArrayList<>());
            TimeSlot requested = reservation.getSlot();
            boolean overlaps = existing.stream().anyMatch(current -> current.getSlot().overlaps(requested));
            if (overlaps) {
                throw new ReservationConflictException("Table " + tableId + " already has an overlapping reservation");
            }
            existing.add(reservation);
        }
    }

    public List<Reservation> findByTable(String tableId) {
        Object lock = locksByTable.computeIfAbsent(tableId, ignored -> new Object());
        synchronized (lock) {
            return List.copyOf(reservationsByTable.getOrDefault(tableId, List.of()));
        }
    }
}
