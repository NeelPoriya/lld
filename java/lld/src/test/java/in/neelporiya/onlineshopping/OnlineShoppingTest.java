package in.neelporiya.onlineshopping;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineShoppingTest {

    private final MutableClock clock = MutableClock.atEpoch();
    private final AtomicInteger ids = new AtomicInteger();

    private ShoppingService service() {
        return ShoppingService.builder()
                .clock(clock)
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .build();
    }

    private Product book() {
        return new Product("book", "Clean Code Book", "A programming book", 1_000,
                Set.of("book", "software"));
    }

    @Test
    void addToCartAndCheckoutCreatesPaidOrderAndDecrementsStock() {
        ShoppingService service = service();
        service.addProduct(book());
        service.addStock("book", 2);

        Cart cart = service.createCart("customer-1");
        service.addToCart(cart.getId(), "book", 1);
        Order order = service.checkout(cart.getId());

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(1_000, order.getTotalCents());
        assertEquals(1, service.inventory().quantityOf("book"));
        assertEquals(order.getId(), order.getPaymentReceipt().orderId());
    }

    @Test
    void insufficientStockIsRejectedAndConsumesNothing() {
        ShoppingService service = service();
        service.addProduct(book());
        service.addStock("book", 1);

        Cart cart = service.createCart("customer-1");
        service.addToCart(cart.getId(), "book", 2);

        assertThrows(InsufficientStockException.class, () -> service.checkout(cart.getId()));
        assertEquals(1, service.inventory().quantityOf("book"));
    }

    @Test
    void legalLifecycleTransitionsWorkAndIllegalTransitionIsRejected() {
        ShoppingService service = service();
        service.addProduct(book());
        service.addStock("book", 1);

        Cart cart = service.createCart("customer-1");
        service.addToCart(cart.getId(), "book", 1);
        Order order = service.checkout(cart.getId());

        service.updateOrderStatus(order.getId(), OrderStatus.SHIPPED);
        service.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertThrows(InvalidOrderTransitionException.class,
                () -> service.updateOrderStatus(order.getId(), OrderStatus.CANCELLED));
    }

    @Test
    void cancelRestocksItemsAndNotifiesObservers() {
        List<OrderStatus> notifications = new ArrayList<>();
        ShoppingService service = ShoppingService.builder()
                .clock(clock)
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .addOrderStatusListener((order, oldStatus, newStatus) -> notifications.add(newStatus))
                .build();
        service.addProduct(book());
        service.addStock("book", 3);

        Cart cart = service.createCart("customer-1");
        service.addToCart(cart.getId(), "book", 2);
        Order order = service.checkout(cart.getId());

        service.cancelOrder(order.getId());

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(3, service.inventory().quantityOf("book"));
        assertEquals(List.of(OrderStatus.PAID, OrderStatus.CANCELLED), notifications);
    }

    @Test
    void discountAndTaxStrategyAreAppliedToTotal() {
        ShoppingService service = ShoppingService.builder()
                .clock(clock)
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .pricingStrategy(new PercentageDiscountTaxPricingStrategy(10, 500))
                .build();
        Product phone = new Product("phone", "Phone", "Smart phone", 10_000, Set.of("electronics"));
        service.addProduct(phone);
        service.addStock("phone", 1);

        Cart cart = service.createCart("customer-1");
        service.addToCart(cart.getId(), "phone", 1);

        assertEquals(9_450, service.checkout(cart.getId()).getTotalCents());
    }

    @Test
    void searchCatalogByNameDescriptionOrKeyword() {
        ShoppingService service = service();
        service.addProduct(new Product("phone", "Phone", "Smart phone", 10_000, Set.of("electronics")));
        service.addProduct(new Product("book", "Novel", "Paperback mystery", 900, Set.of("reading")));

        List<Product> electronics = service.search("elect");
        List<Product> phone = service.search("pho");

        assertEquals(List.of("phone"), electronics.stream().map(Product::id).toList());
        assertEquals(List.of("phone"), phone.stream().map(Product::id).toList());
        assertTrue(service.search("missing").isEmpty());
    }
}
