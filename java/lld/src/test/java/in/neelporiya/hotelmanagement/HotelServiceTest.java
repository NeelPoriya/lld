package in.neelporiya.hotelmanagement;

import in.neelporiya.hotelmanagement.exception.IllegalReservationTransitionException;
import in.neelporiya.hotelmanagement.exception.RoomNotAvailableException;
import in.neelporiya.hotelmanagement.model.Reservation;
import in.neelporiya.hotelmanagement.model.ReservationStatus;
import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.RoomFactory;
import in.neelporiya.hotelmanagement.model.RoomType;
import in.neelporiya.hotelmanagement.model.StayRange;
import in.neelporiya.hotelmanagement.observer.ReservationEventListener;
import in.neelporiya.hotelmanagement.pricing.NightlyRatePricingStrategy;
import in.neelporiya.hotelmanagement.pricing.TaxDiscountPricingStrategy;
import in.neelporiya.hotelmanagement.search.DefaultRoomSearchStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelServiceTest {

    private final AtomicInteger ids = new AtomicInteger();
    private final MutableClock clock = MutableClock.atEpoch();

    private HotelService service() {
        return new HotelService(
                clock,
                () -> "res-" + ids.incrementAndGet(),
                new NightlyRatePricingStrategy(),
                new DefaultRoomSearchStrategy());
    }

    private StayRange range(int startDay, int endDay) {
        return new StayRange(LocalDate.of(2026, 8, startDay), LocalDate.of(2026, 8, endDay));
    }

    private Room room(String id, RoomType type, int floor) {
        return RoomFactory.create(id, id.substring(id.length() - 3), type, floor);
    }

    @Test
    void searchReturnsOnlyAvailableRoomsForRange() {
        HotelService service = service();
        service.addRoom(room("room-101", RoomType.STANDARD, 1));
        service.addRoom(room("room-102", RoomType.STANDARD, 1));
        service.addRoom(room("room-201", RoomType.SUITE, 2));

        service.bookRoom("guest-1", "room-101", range(1, 5));

        var available = service.searchAvailable(range(2, 4), Optional.of(RoomType.STANDARD));

        assertEquals(1, available.size());
        assertEquals("room-102", available.getFirst().getId());
    }

    @Test
    void booksSpecificRoomWithDeterministicCreatedAtAndPrice() {
        HotelService service = service();
        service.addRoom(room("room-201", RoomType.DELUXE, 2));

        Reservation reservation = service.bookRoom("guest-1", "room-201", range(1, 4));

        assertEquals("res-1", reservation.getId());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(clock.instant(), reservation.getCreatedAt());
        assertEquals(54_000, reservation.getQuotedPriceCents());
    }

    @Test
    void overlappingBookingForSameRoomIsRejectedButNonOverlappingSucceeds() {
        HotelService service = service();
        service.addRoom(room("room-301", RoomType.SUITE, 3));

        service.bookRoom("guest-1", "room-301", range(10, 15));

        assertThrows(RoomNotAvailableException.class,
                () -> service.bookRoom("guest-2", "room-301", range(14, 18)));

        Reservation adjacent = service.bookRoom("guest-3", "room-301", range(15, 18));
        assertEquals(ReservationStatus.CONFIRMED, adjacent.getStatus());
    }

    @Test
    void bookRoomOfTypePicksAnAvailableRoom() {
        HotelService service = service();
        service.addRoom(room("room-101", RoomType.STANDARD, 1));
        service.addRoom(room("room-102", RoomType.STANDARD, 1));
        service.bookRoom("guest-1", "room-101", range(1, 5));

        Reservation reservation = service.bookRoomOfType("guest-2", RoomType.STANDARD, range(2, 4));

        assertEquals("room-102", reservation.getRoomId());
    }

    @Test
    void pricingUsesNightsRateTaxAndDiscountWithoutDouble() {
        var pricing = new TaxDiscountPricingStrategy(new NightlyRatePricingStrategy(), 1_000, 2_000);
        Room suite = room("room-501", RoomType.SUITE, 5);

        long price = pricing.calculateCents(suite, range(1, 3));

        assertEquals(61_600, price);
    }

    @Test
    void checkInAndCheckOutFollowLegalLifecycle() {
        HotelService service = service();
        service.addRoom(room("room-101", RoomType.STANDARD, 1));
        Reservation reservation = service.bookRoom("guest-1", "room-101", range(1, 2));

        clock.advance(Duration.ofHours(2));
        service.checkIn(reservation.getId());
        assertEquals(ReservationStatus.CHECKED_IN, reservation.getStatus());
        assertNotNull(reservation.getCheckedInAt());

        clock.advance(Duration.ofHours(5));
        service.checkOut(reservation.getId());
        assertEquals(ReservationStatus.CHECKED_OUT, reservation.getStatus());
        assertNotNull(reservation.getCheckedOutAt());

        assertThrows(IllegalReservationTransitionException.class, () -> service.cancel(reservation.getId()));
    }

    @Test
    void illegalCheckOutBeforeCheckInIsRejected() {
        HotelService service = service();
        service.addRoom(room("room-101", RoomType.STANDARD, 1));
        Reservation reservation = service.bookRoom("guest-1", "room-101", range(1, 2));

        assertThrows(IllegalReservationTransitionException.class, () -> service.checkOut(reservation.getId()));
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void cancellationFreesTheRangeAndNotifiesObservers() {
        HotelService service = service();
        service.addRoom(room("room-201", RoomType.DELUXE, 2));
        List<String> events = new ArrayList<>();
        service.addListener(new ReservationEventListener() {
            @Override
            public void onBooked(Reservation reservation) {
                events.add("booked:" + reservation.getId());
            }

            @Override
            public void onCancelled(Reservation reservation) {
                events.add("cancelled:" + reservation.getId());
            }
        });
        Reservation first = service.bookRoom("guest-1", "room-201", range(1, 4));

        service.cancel(first.getId());

        assertEquals(ReservationStatus.CANCELLED, first.getStatus());
        assertTrue(service.isAvailable("room-201", range(2, 3)));
        Reservation second = service.bookRoom("guest-2", "room-201", range(2, 3));
        assertEquals(ReservationStatus.CONFIRMED, second.getStatus());
        assertEquals(List.of("booked:res-1", "cancelled:res-1", "booked:res-2"), events);
    }
}
