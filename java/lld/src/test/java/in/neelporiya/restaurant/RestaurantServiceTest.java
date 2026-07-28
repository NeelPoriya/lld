package in.neelporiya.restaurant;

import in.neelporiya.restaurant.billing.BillCalculator;
import in.neelporiya.restaurant.billing.FixedDiscountStrategy;
import in.neelporiya.restaurant.billing.FixedTipStrategy;
import in.neelporiya.restaurant.billing.PercentageTaxStrategy;
import in.neelporiya.restaurant.event.KitchenDisplay;
import in.neelporiya.restaurant.exception.InvalidOrderTransitionException;
import in.neelporiya.restaurant.exception.MenuItemNotFoundException;
import in.neelporiya.restaurant.exception.ReservationConflictException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class RestaurantServiceTest {
    private final MutableClock clock = MutableClock.atEpoch();
    private final AtomicInteger orderIds = new AtomicInteger();
    private final AtomicInteger reservationIds = new AtomicInteger();

    private RestaurantService service() {
        RestaurantService service = RestaurantService.builder()
                .clock(clock)
                .orderIdGenerator(() -> "O-" + orderIds.incrementAndGet())
                .reservationIdGenerator(() -> "R-" + reservationIds.incrementAndGet())
                .billCalculator(new BillCalculator(
                        new PercentageTaxStrategy(1000),
                        new FixedDiscountStrategy(300),
                        new FixedTipStrategy(150)))
                .build();
        service.addTable(new Table("T1", 4));
        service.addMenuItem(new MenuItem("paneer", "Paneer Tikka", 1_000));
        service.addMenuItem(new MenuItem("naan", "Butter Naan", 500));
        return service;
    }

    @Test
    void placeOrderCreatesLineItemsAndUsesInjectedTimestamp() {
        RestaurantService service = service();

        Order order = service.placeOrder("T1", List.of(
                new OrderRequestItem("paneer", 1),
                new OrderRequestItem("naan", 2)));

        Assertions.assertEquals("O-1", order.getId());
        Assertions.assertEquals(OrderStatus.PLACED, order.getStatus());
        Assertions.assertEquals(Instant.EPOCH, order.getPlacedAt());
        Assertions.assertEquals(2_000, order.subtotalCents());
    }

    @Test
    void orderLifecycleAllowsLegalTransitionsAndRejectsIllegalTransition() {
        RestaurantService service = service();
        Order order = service.placeOrder("T1", List.of(new OrderRequestItem("paneer", 1)));

        clock.advance(Duration.ofMinutes(5));
        service.changeOrderStatus(order.getId(), OrderStatus.PREPARING);
        clock.advance(Duration.ofMinutes(10));
        service.changeOrderStatus(order.getId(), OrderStatus.READY);
        clock.advance(Duration.ofMinutes(2));
        service.changeOrderStatus(order.getId(), OrderStatus.SERVED);
        clock.advance(Duration.ofMinutes(1));
        service.changeOrderStatus(order.getId(), OrderStatus.PAID);

        Assertions.assertEquals(OrderStatus.PAID, order.getStatus());
        Assertions.assertNotNull(order.getReadyAt());
        Assertions.assertNotNull(order.getServedAt());
        Assertions.assertNotNull(order.getPaidAt());
        Assertions.assertThrows(InvalidOrderTransitionException.class,
                () -> service.changeOrderStatus(order.getId(), OrderStatus.SERVED));
    }

    @Test
    void billUsesTaxDiscountAndTipStrategies() {
        RestaurantService service = service();
        Order order = service.placeOrder("T1", List.of(
                new OrderRequestItem("paneer", 1),
                new OrderRequestItem("naan", 2)));

        Bill bill = service.bill(order.getId());

        Assertions.assertEquals(2_000, bill.subtotalCents());
        Assertions.assertEquals(200, bill.taxCents());
        Assertions.assertEquals(300, bill.discountCents());
        Assertions.assertEquals(150, bill.tipCents());
        Assertions.assertEquals(2_050, bill.totalCents());
    }

    @Test
    void reservationsRejectOverlapsAndAllowTouchingSlots() {
        RestaurantService service = service();
        TimeSlot dinner = new TimeSlot(Instant.EPOCH.plus(Duration.ofHours(18)), Instant.EPOCH.plus(Duration.ofHours(20)));
        TimeSlot overlap = new TimeSlot(Instant.EPOCH.plus(Duration.ofHours(19)), Instant.EPOCH.plus(Duration.ofHours(21)));
        TimeSlot later = new TimeSlot(Instant.EPOCH.plus(Duration.ofHours(20)), Instant.EPOCH.plus(Duration.ofHours(22)));

        Reservation first = service.reserveTable("T1", "Neel", dinner);

        Assertions.assertEquals("R-1", first.getId());
        Assertions.assertThrows(ReservationConflictException.class, () -> service.reserveTable("T1", "Other", overlap));
        Assertions.assertEquals("R-3", service.reserveTable("T1", "Later", later).getId());
        Assertions.assertEquals(2, service.reservationsForTable("T1").size());
    }

    @Test
    void menuLookupReturnsItemsAndRejectsUnknownIds() {
        RestaurantService service = service();

        Assertions.assertEquals("Paneer Tikka", service.menuItem("paneer").getName());
        Assertions.assertThrows(MenuItemNotFoundException.class, () -> service.menuItem("missing"));
    }

    @Test
    void observerIsNotifiedOnOrderPlacementAndStateChange() {
        KitchenDisplay display = new KitchenDisplay();
        RestaurantService service = RestaurantService.builder()
                .clock(clock)
                .orderIdGenerator(() -> "O-1")
                .addListener(display)
                .build();
        service.addTable(new Table("T1", 2));
        service.addMenuItem(new MenuItem("tea", "Tea", 100));

        Order order = service.placeOrder("T1", List.of(new OrderRequestItem("tea", 1)));
        service.changeOrderStatus(order.getId(), OrderStatus.PREPARING);

        Assertions.assertEquals(List.of("placed:O-1", "PLACED->PREPARING:O-1"), display.events());
    }
}
