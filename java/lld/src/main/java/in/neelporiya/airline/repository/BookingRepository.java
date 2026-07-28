package in.neelporiya.airline.repository;

import in.neelporiya.airline.model.Booking;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BookingRepository {

    private final ConcurrentMap<String, Booking> bookingsById = new ConcurrentHashMap<>();

    public void save(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        bookingsById.put(booking.getId(), booking);
    }

    public Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookingsById.get(id));
    }

    public List<Booking> findAll() {
        return List.copyOf(bookingsById.values());
    }
}
