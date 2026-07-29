package in.neelporiya.fooddelivery;

import in.neelporiya.fooddelivery.exception.EmptyCartException;
import in.neelporiya.fooddelivery.exception.InvalidOrderTransitionException;
import in.neelporiya.fooddelivery.exception.ItemUnavailableException;
import in.neelporiya.fooddelivery.exception.NoAgentAvailableException;
import in.neelporiya.fooddelivery.exception.RestaurantClosedException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodDeliveryServiceTest {

    private FoodDeliveryService app;
    private Restaurant dosaPlace;
    private MenuItem dosa;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        app = new FoodDeliveryService(
                new StandardPricing(new BigDecimal("20"), new BigDecimal("5"), new BigDecimal("0.05")),
                new NearestAgentStrategy(), MutableClock.atEpoch(), () -> "id" + seq.incrementAndGet());
        dosaPlace = app.registerRestaurant("Dosa Place", new Location(0, 0));
        dosa = dosaPlace.getMenu().addItem(new MenuItem("m1", "Masala Dosa", new BigDecimal("100")));
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private Customer customerAt(double x, double y) {
        return app.registerCustomer("alice", new Location(x, y));
    }

    @Test
    void placeOrderPricesSubtotalDeliveryAndTax() {
        Customer c = customerAt(3, 4); // distance 5 from (0,0)
        Cart cart = app.newCart(dosaPlace.getId()).add(dosa, 2);

        Order order = app.placeOrder(c.id(), cart);

        Bill bill = order.getBill();
        assertMoney("200.00", bill.subtotal());
        assertMoney("45.00", bill.deliveryFee()); // 20 + 5*5
        assertMoney("10.00", bill.tax());          // 200 * 0.05
        assertMoney("255.00", bill.total());
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }

    @Test
    void fullHappyPathThroughDelivery() {
        Customer c = customerAt(3, 4);
        DeliveryAgent rider = app.registerAgent("rider-1", new Location(1, 0));
        Order order = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));

        List<OrderStatus> transitions = new ArrayList<>();
        app.addListener(new OrderListener() {
            @Override
            public void onStatusChanged(Order o, OrderStatus from, OrderStatus to) {
                transitions.add(to);
            }
        });

        app.acceptOrder(order.getId());
        app.startPreparing(order.getId());
        app.markReady(order.getId());
        DeliveryAgent assigned = app.dispatch(order.getId());
        app.deliver(order.getId());

        assertSame(rider, assigned);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertTrue(rider.isAvailable(), "rider freed after delivery");
        assertEquals(List.of(OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP,
                OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED), transitions);
    }

    @Test
    void nearestAgentIsChosen() {
        Customer c = customerAt(3, 4);
        DeliveryAgent far = app.registerAgent("far", new Location(10, 0));
        DeliveryAgent near = app.registerAgent("near", new Location(1, 0));
        Order order = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));
        app.acceptOrder(order.getId());
        app.startPreparing(order.getId());
        app.markReady(order.getId());

        assertSame(near, app.dispatch(order.getId()));
        assertFalse(near.isAvailable());
        assertTrue(far.isAvailable());
    }

    @Test
    void closedRestaurantRejectsOrders() {
        Customer c = customerAt(3, 4);
        Cart cart = app.newCart(dosaPlace.getId()).add(dosa, 1);
        dosaPlace.close();
        assertThrows(RestaurantClosedException.class, () -> app.placeOrder(c.id(), cart));
    }

    @Test
    void emptyCartIsRejected() {
        Customer c = customerAt(3, 4);
        assertThrows(EmptyCartException.class, () -> app.placeOrder(c.id(), app.newCart(dosaPlace.getId())));
    }

    @Test
    void unavailableItemIsRejected() {
        Customer c = customerAt(3, 4);
        Cart cart = app.newCart(dosaPlace.getId()).add(dosa, 1);
        dosaPlace.getMenu().setAvailable(dosa.id(), false);
        assertThrows(ItemUnavailableException.class, () -> app.placeOrder(c.id(), cart));
    }

    @Test
    void illegalTransitionsAreRejected() {
        Customer c = customerAt(3, 4);
        Order order = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));
        // Cannot deliver an order that was never dispatched.
        assertThrows(InvalidOrderTransitionException.class, () -> app.deliver(order.getId()));
        // Cannot skip straight to preparing from placed.
        assertThrows(InvalidOrderTransitionException.class, () -> app.startPreparing(order.getId()));
    }

    @Test
    void cancellationAllowedBeforeDispatchButNotAfter() {
        Customer c = customerAt(3, 4);
        app.registerAgent("rider", new Location(1, 0));
        Order first = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));
        app.acceptOrder(first.getId());
        app.cancelOrder(first.getId());
        assertEquals(OrderStatus.CANCELLED, first.getStatus());

        Order second = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));
        app.acceptOrder(second.getId());
        app.startPreparing(second.getId());
        app.markReady(second.getId());
        app.dispatch(second.getId());
        assertThrows(InvalidOrderTransitionException.class, () -> app.cancelOrder(second.getId()));
    }

    @Test
    void dispatchWithNoFreeAgentThrows() {
        Customer c = customerAt(3, 4);
        Order order = app.placeOrder(c.id(), app.newCart(dosaPlace.getId()).add(dosa, 1));
        app.acceptOrder(order.getId());
        app.startPreparing(order.getId());
        app.markReady(order.getId());
        assertThrows(NoAgentAvailableException.class, () -> app.dispatch(order.getId()));
    }

    @Test
    void searchFindsOpenRestaurantsByNameOrItem() {
        Restaurant pizza = app.registerRestaurant("Pizza Hub", new Location(5, 5));
        pizza.getMenu().addItem(new MenuItem("p1", "Margherita", new BigDecimal("250")));

        assertEquals(List.of(dosaPlace), app.searchRestaurants("dosa"));       // by name
        assertEquals(List.of(pizza), app.searchRestaurants("margherita"));      // by item
        pizza.close();
        assertTrue(app.searchRestaurants("margherita").isEmpty(), "closed restaurants are hidden");
    }
}
