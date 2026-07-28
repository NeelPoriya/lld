package in.neelporiya.airline;

import in.neelporiya.airline.exception.IllegalBookingTransitionException;
import in.neelporiya.airline.exception.SeatUnavailableException;
import in.neelporiya.airline.model.Aircraft;
import in.neelporiya.airline.model.AircraftBuilder;
import in.neelporiya.airline.model.Booking;
import in.neelporiya.airline.model.BookingStatus;
import in.neelporiya.airline.model.Flight;
import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Passenger;
import in.neelporiya.airline.model.SeatClass;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlineServiceTest {

    private final AtomicInteger ids = new AtomicInteger();
    private final MutableClock clock = MutableClock.atEpoch();

    private AirlineService service() {
        return AirlineService.builder()
                .clock(clock)
                .idGenerator(() -> "booking-" + ids.incrementAndGet())
                .build();
    }

    private Passenger passenger(String id) {
        return new Passenger(id, "Passenger " + id);
    }

    private FlightInstance addInstance(AirlineService service, String instanceId, String origin, String destination, LocalDate date) {
        Flight flight = new Flight("NP-" + instanceId, origin, destination);
        Aircraft aircraft = AircraftBuilder.forTailNumber("VT-" + instanceId)
                .addSeat("E1", SeatClass.ECONOMY, 10_000)
                .addSeat("E2", SeatClass.ECONOMY, 10_000)
                .addSeat("B1", SeatClass.BUSINESS, 10_000)
                .addSeat("F1", SeatClass.FIRST, 10_000)
                .build();
        FlightInstance instance = new FlightInstance(instanceId, flight, date, aircraft);
        service.addFlight(flight);
        service.addFlightInstance(instance);
        return instance;
    }

    @Test
    void searchFlightsByOriginDestinationAndDate() {
        AirlineService service = service();
        LocalDate date = LocalDate.of(2026, 8, 1);
        addInstance(service, "inst-1", "BLR", "DEL", date);
        addInstance(service, "inst-2", "BLR", "BOM", date);
        addInstance(service, "inst-3", "BLR", "DEL", date.plusDays(1));

        var results = service.searchFlights("BLR", "DEL", date);

        assertEquals(1, results.size());
        assertEquals("inst-1", results.getFirst().getId());
    }

    @Test
    void booksSpecificSeatWithDeterministicIdFareAndTimestamp() {
        AirlineService service = service();
        addInstance(service, "inst-1", "BLR", "DEL", LocalDate.of(2026, 8, 1));

        Booking booking = service.bookSeat(passenger("p1"), "inst-1", "E1");

        assertEquals("booking-1", booking.getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("E1", booking.getSeatNumber());
        assertEquals(clock.instant(), booking.getCreatedAt());
        assertEquals(new BigDecimal("100.00"), booking.getFare());
    }

    @Test
    void doubleBookingSameSeatIsRejected() {
        AirlineService service = service();
        addInstance(service, "inst-1", "BLR", "DEL", LocalDate.of(2026, 8, 1));

        service.bookSeat(passenger("p1"), "inst-1", "E1");

        assertThrows(SeatUnavailableException.class, () -> service.bookSeat(passenger("p2"), "inst-1", "E1"));
    }

    @Test
    void cancelFreesSeatForRebooking() {
        AirlineService service = service();
        addInstance(service, "inst-1", "BLR", "DEL", LocalDate.of(2026, 8, 1));
        Booking first = service.bookSeat(passenger("p1"), "inst-1", "E1");

        service.cancel(first.getId());
        Booking second = service.bookSeat(passenger("p2"), "inst-1", "E1");

        assertEquals(BookingStatus.CANCELLED, first.getStatus());
        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
        assertEquals("E1", second.getSeatNumber());
    }

    @Test
    void fareDiffersBySeatClassWithoutDoubleArithmetic() {
        AirlineService service = service();
        addInstance(service, "inst-1", "BLR", "DEL", LocalDate.of(2026, 8, 1));

        Booking economy = service.bookSeat(passenger("p1"), "inst-1", "E1");
        Booking business = service.bookSeat(passenger("p2"), "inst-1", "B1");
        Booking first = service.bookSeat(passenger("p3"), "inst-1", "F1");

        assertEquals(new BigDecimal("100.00"), economy.getFare());
        assertEquals(new BigDecimal("200.00"), business.getFare());
        assertEquals(new BigDecimal("400.00"), first.getFare());
        assertTrue(first.getFare().compareTo(business.getFare()) > 0);
    }

    @Test
    void bookingLifecycleFollowsLegalTransitionsAndRejectsIllegalOnes() {
        AirlineService service = service();
        addInstance(service, "inst-1", "BLR", "DEL", LocalDate.of(2026, 8, 1));
        Booking booking = service.bookSeat(passenger("p1"), "inst-1", "E1");

        clock.advance(Duration.ofHours(2));
        service.checkIn(booking.getId());
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
        assertNotNull(booking.getCheckedInAt());

        clock.advance(Duration.ofHours(1));
        service.cancel(booking.getId());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertNotNull(booking.getCancelledAt());

        assertThrows(IllegalBookingTransitionException.class, () -> service.checkIn(booking.getId()));
        assertThrows(IllegalBookingTransitionException.class, () -> service.cancel(booking.getId()));
    }
}
