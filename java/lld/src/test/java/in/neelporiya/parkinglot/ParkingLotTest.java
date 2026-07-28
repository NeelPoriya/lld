package in.neelporiya.parkinglot;

import in.neelporiya.parkinglot.exception.InvalidTicketException;
import in.neelporiya.parkinglot.exception.NoSpotAvailableException;
import in.neelporiya.parkinglot.observer.ParkingEventListener;
import in.neelporiya.parkinglot.spot.ParkingSpot;
import in.neelporiya.parkinglot.spot.ParkingSpotType;
import in.neelporiya.parkinglot.vehicle.Car;
import in.neelporiya.parkinglot.vehicle.Motorcycle;
import in.neelporiya.parkinglot.vehicle.Truck;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingLotTest {

    private final MutableClock clock = MutableClock.atEpoch();

    /** Builds a lot with the requested number of spots of each type, distance = insertion order. */
    private ParkingLot lotWith(int motorcycle, int compact, int large) {
        List<ParkingSpot> spots = new ArrayList<>();
        int distance = 0;
        for (int i = 0; i < motorcycle; i++) {
            spots.add(new ParkingSpot("M" + i, ParkingSpotType.MOTORCYCLE, 1, distance++));
        }
        for (int i = 0; i < compact; i++) {
            spots.add(new ParkingSpot("C" + i, ParkingSpotType.COMPACT, 1, distance++));
        }
        for (int i = 0; i < large; i++) {
            spots.add(new ParkingSpot("L" + i, ParkingSpotType.LARGE, 1, distance++));
        }
        return ParkingLot.builder()
                .clock(clock)
                .addFloor(new ParkingFloor(1, spots))
                .build();
    }

    @Test
    void parkIssuesTicketAndReducesAvailability() {
        ParkingLot lot = lotWith(0, 2, 0);

        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-1111"));

        assertNotNull(ticket.getId());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(1L, lot.availability().get(ParkingSpotType.COMPACT));
        assertEquals(1, lot.activeTicketCount());
    }

    @Test
    void feeIsComputedFromInjectedClock() {
        // TESTABILITY: we never sleep. We advance the clock 3 hours and assert the fee instantly.
        ParkingLot lot = lotWith(0, 1, 0);
        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-2222"));

        clock.advance(Duration.ofHours(3));
        ParkingReceipt receipt = lot.unpark(ticket.getId());

        // Default CAR rate is 20/hour -> 3 hours -> 60.
        assertEquals(0, new BigDecimal("60").compareTo(receipt.fee()));
        assertEquals(TicketStatus.PAID, ticket.getStatus());
    }

    @Test
    void partialHourRoundsUpAndHasOneHourMinimum() {
        ParkingLot lot = lotWith(0, 1, 0);
        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-3333"));

        clock.advance(Duration.ofMinutes(1)); // 1 minute still bills a full hour
        ParkingReceipt receipt = lot.unpark(ticket.getId());

        assertEquals(0, new BigDecimal("20").compareTo(receipt.fee()));
    }

    @Test
    void unparkFreesTheSpotForReuse() {
        ParkingLot lot = lotWith(0, 1, 0);
        ParkingTicket first = lot.parkVehicle(new Car("KA-01-4444"));
        lot.unpark(first.getId());

        // The single spot is free again, so a second car can park.
        ParkingTicket second = lot.parkVehicle(new Car("KA-01-5555"));
        assertNotNull(second);
        assertEquals(0L, lot.availability().getOrDefault(ParkingSpotType.COMPACT, 0L));
    }

    @Test
    void motorcyclePrefersSmallestFittingSpot() {
        // A motorcycle fits every spot type; the nearest-then-smallest strategy should still land it
        // on the MOTORCYCLE spot (distance 0), leaving COMPACT/LARGE free for bigger vehicles.
        ParkingLot lot = lotWith(1, 1, 1);

        ParkingTicket ticket = lot.parkVehicle(new Motorcycle("KA-02-0001"));

        assertEquals(ParkingSpotType.MOTORCYCLE, ticket.getSpot().getType());
    }

    @Test
    void truckOnlyFitsLargeSpot() {
        ParkingLot lot = lotWith(5, 5, 1); // lots of small spots, one large

        ParkingTicket ticket = lot.parkVehicle(new Truck("KA-03-0001"));

        assertEquals(ParkingSpotType.LARGE, ticket.getSpot().getType());
    }

    @Test
    void throwsWhenNoFittingSpot() {
        ParkingLot lot = lotWith(2, 0, 0); // only motorcycle spots

        assertThrows(NoSpotAvailableException.class, () -> lot.parkVehicle(new Truck("KA-03-9999")));
    }

    @Test
    void unknownTicketThrows() {
        ParkingLot lot = lotWith(0, 1, 0);
        assertThrows(InvalidTicketException.class, () -> lot.unpark("does-not-exist"));
    }

    @Test
    void redeemingSameTicketTwiceThrows() {
        ParkingLot lot = lotWith(0, 1, 0);
        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-7777"));
        lot.unpark(ticket.getId());

        assertThrows(InvalidTicketException.class, () -> lot.unpark(ticket.getId()));
    }

    @Test
    void deterministicTicketIdsViaInjectedGenerator() {
        // TESTABILITY: id generation is injected, so we can assert exact ids instead of UUIDs.
        AtomicInteger counter = new AtomicInteger();
        ParkingLot lot = ParkingLot.builder()
                .clock(clock)
                .ticketIdGenerator(() -> "T-" + counter.incrementAndGet())
                .addFloor(new ParkingFloor(1,
                        List.of(new ParkingSpot("C0", ParkingSpotType.COMPACT, 1, 0))))
                .build();

        assertEquals("T-1", lot.parkVehicle(new Car("KA-01-0001")).getId());
    }

    @Test
    void observerIsNotifiedOnParkAndUnpark() {
        // DESIGN PATTERN: Observer — the listener reacts without the lot knowing its concrete type.
        List<String> events = new ArrayList<>();
        ParkingEventListener listener = new ParkingEventListener() {
            @Override
            public void onVehicleParked(ParkingTicket ticket) {
                events.add("park:" + ticket.getVehicle().getLicensePlate());
            }

            @Override
            public void onVehicleUnparked(ParkingReceipt receipt) {
                events.add("unpark:" + receipt.licensePlate());
            }
        };
        ParkingLot lot = ParkingLot.builder()
                .clock(clock)
                .addListener(listener)
                .addFloor(new ParkingFloor(1,
                        List.of(new ParkingSpot("C0", ParkingSpotType.COMPACT, 1, 0))))
                .build();

        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-8888"));
        lot.unpark(ticket.getId());

        assertEquals(List.of("park:KA-01-8888", "unpark:KA-01-8888"), events);
    }

    @Test
    void ticketRetainsSpotReference() {
        ParkingLot lot = lotWith(0, 1, 0);
        ParkingTicket ticket = lot.parkVehicle(new Car("KA-01-1212"));
        assertSame(ticket, lot.findActiveTicket(ticket.getId()).orElseThrow());
        assertTrue(ticket.getSpot().getId().startsWith("C"));
    }
}
