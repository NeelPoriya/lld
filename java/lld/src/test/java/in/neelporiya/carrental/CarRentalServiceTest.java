package in.neelporiya.carrental;

import in.neelporiya.carrental.exception.IllegalReservationTransitionException;
import in.neelporiya.carrental.exception.VehicleNotAvailableException;
import in.neelporiya.carrental.model.AddOn;
import in.neelporiya.carrental.model.DateRange;
import in.neelporiya.carrental.model.Reservation;
import in.neelporiya.carrental.model.ReservationStatus;
import in.neelporiya.carrental.model.Vehicle;
import in.neelporiya.carrental.model.VehicleFactory;
import in.neelporiya.carrental.model.VehicleType;
import in.neelporiya.carrental.pricing.PerDayPricingStrategy;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarRentalServiceTest {

    private final AtomicInteger ids = new AtomicInteger();
    private final MutableClock clock = MutableClock.atEpoch();

    private CarRentalService service() {
        return new CarRentalService(clock, () -> "res-" + ids.incrementAndGet(), new PerDayPricingStrategy());
    }

    private DateRange range(int startDay, int endDay) {
        return new DateRange(LocalDate.of(2026, 8, startDay), LocalDate.of(2026, 8, endDay));
    }

    private Vehicle vehicle(String id, VehicleType type, String storeId) {
        return VehicleFactory.create(id, type, "KA-" + id, storeId, "Toyota", "Model-" + id);
    }

    @Test
    void searchReturnsOnlyAvailableVehiclesForRange() {
        CarRentalService service = service();
        service.addVehicle(vehicle("eco-1", VehicleType.ECONOMY, "BLR"));
        service.addVehicle(vehicle("eco-2", VehicleType.ECONOMY, "BLR"));
        service.addVehicle(vehicle("suv-1", VehicleType.SUV, "DEL"));

        service.reserve("cust-1", "eco-1", range(1, 5), Set.of());

        var available = service.searchAvailable("BLR", range(2, 4), Optional.of(VehicleType.ECONOMY));

        assertEquals(1, available.size());
        assertEquals("eco-2", available.getFirst().getId());
    }

    @Test
    void reservesSpecificVehicleWithDeterministicCreatedAtAndPrice() {
        CarRentalService service = service();
        service.addVehicle(vehicle("suv-1", VehicleType.SUV, "BLR"));

        Reservation reservation = service.reserve("cust-1", "suv-1", range(1, 4), Set.of(AddOn.GPS));

        assertEquals("res-1", reservation.getId());
        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
        assertEquals(clock.instant(), reservation.getCreatedAt());
        assertEquals(new BigDecimal("165.00"), reservation.getQuotedPrice());
    }

    @Test
    void overlappingReservationForSameVehicleIsRejectedButNonOverlappingSucceeds() {
        CarRentalService service = service();
        service.addVehicle(vehicle("lux-1", VehicleType.LUXURY, "BLR"));

        service.reserve("cust-1", "lux-1", range(10, 15), Set.of());

        assertThrows(VehicleNotAvailableException.class,
                () -> service.reserve("cust-2", "lux-1", range(14, 18), Set.of()));

        Reservation adjacent = service.reserve("cust-3", "lux-1", range(15, 18), Set.of());
        assertEquals(ReservationStatus.RESERVED, adjacent.getStatus());
    }

    @Test
    void pricingUsesDaysVehicleTypeAndAddOnsWithoutDouble() {
        PerDayPricingStrategy pricing = new PerDayPricingStrategy();
        Vehicle van = vehicle("van-1", VehicleType.VAN, "BLR");

        BigDecimal price = pricing.calculate(van, range(1, 3), Set.of(AddOn.INSURANCE, AddOn.CHILD_SEAT));

        assertEquals(new BigDecimal("188.00"), price);
    }

    @Test
    void pickupAndReturnFollowLegalLifecycle() {
        CarRentalService service = service();
        service.addVehicle(vehicle("eco-1", VehicleType.ECONOMY, "BLR"));
        Reservation reservation = service.reserve("cust-1", "eco-1", range(1, 2), Set.of());

        clock.advance(Duration.ofHours(2));
        service.pickUp(reservation.getId());
        assertEquals(ReservationStatus.ONGOING, reservation.getStatus());
        assertNotNull(reservation.getPickedUpAt());

        clock.advance(Duration.ofHours(5));
        service.returnVehicle(reservation.getId());
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertNotNull(reservation.getReturnedAt());

        assertThrows(IllegalReservationTransitionException.class, () -> service.cancel(reservation.getId()));
    }

    @Test
    void illegalReturnBeforePickupIsRejected() {
        CarRentalService service = service();
        service.addVehicle(vehicle("eco-1", VehicleType.ECONOMY, "BLR"));
        Reservation reservation = service.reserve("cust-1", "eco-1", range(1, 2), Set.of());

        assertThrows(IllegalReservationTransitionException.class, () -> service.returnVehicle(reservation.getId()));
        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
    }

    @Test
    void cancellationFreesTheRange() {
        CarRentalService service = service();
        service.addVehicle(vehicle("suv-1", VehicleType.SUV, "BLR"));
        Reservation first = service.reserve("cust-1", "suv-1", range(1, 4), Set.of());

        service.cancel(first.getId());

        assertEquals(ReservationStatus.CANCELLED, first.getStatus());
        assertTrue(service.isAvailable("suv-1", range(2, 3)));
        Reservation second = service.reserve("cust-2", "suv-1", range(2, 3), Set.of());
        assertEquals(ReservationStatus.RESERVED, second.getStatus());
    }
}
