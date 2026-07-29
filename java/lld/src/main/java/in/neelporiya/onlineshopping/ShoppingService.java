package in.neelporiya.onlineshopping;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * The application facade clients use for catalog, cart, checkout, and order lifecycle.
 *
 * <p>// DESIGN PATTERN: Facade — controllers/tests talk to one object while repositories, inventory,
 * pricing, payment, search, and observers remain independent collaborators.
 */
public class ShoppingService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final Inventory inventory;
    private final PricingStrategy pricingStrategy;
    private final PaymentMethod paymentMethod;
    private final CatalogSearchStrategy searchStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final List<OrderStatusListener> listeners;

    private ShoppingService(Builder builder) {
        this.productRepository = builder.productRepository;
        this.cartRepository = builder.cartRepository;
        this.orderRepository = builder.orderRepository;
        this.inventory = builder.inventory;
        this.pricingStrategy = builder.pricingStrategy;
        this.paymentMethod = builder.paymentMethod;
        this.searchStrategy = builder.searchStrategy;
        this.clock = builder.clock;
        this.idGenerator = builder.idGenerator;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
    }

    public void addProduct(Product product) {
        productRepository.save(Objects.requireNonNull(product, "product"));
    }

    public void addStock(String productId, int quantity) {
        inventory.addStock(productId, quantity);
    }

    public List<Product> search(String query) {
        return searchStrategy.search(productRepository.findAll(), query);
    }

    public Cart createCart(String customerId) {
        Cart cart = Cart.builder(idGenerator.get(), customerId).build();
        cartRepository.save(cart);
        return cart;
    }

    public Cart addToCart(String cartId, String productId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Unknown cart: " + cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Unknown product: " + productId));
        cart.addProduct(product, quantity);
        cartRepository.save(cart);
        return cart;
    }

    public Order checkout(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Unknown cart: " + cartId));
        if (cart.items().isEmpty()) {
            throw new ShoppingException("Cannot checkout an empty cart");
        }
        if (!cart.tryCloseForCheckout()) {
            throw new CartAlreadyCheckedOutException("Cart is already checked out: " + cartId);
        }

        if (!inventory.tryReserve(cart.quantitiesByProductId())) {
            cart.reopenAfterFailedCheckout();
            throw new InsufficientStockException("Insufficient stock for cart: " + cartId);
        }

        try {
            Order order = buildPlacedOrder(cart);
            PaymentReceipt receipt = paymentMethod.pay(new PaymentRequest(order.getId(), order.getTotalCents()));
            if (!receipt.approved()) {
                inventory.restock(cart.quantitiesByProductId());
                cart.reopenAfterFailedCheckout();
                throw new PaymentFailedException("Payment declined for order: " + order.getId());
            }
            order.markPaid(receipt);
            orderRepository.save(order);
            notifyStatusChanged(order, OrderStatus.PLACED, OrderStatus.PAID);
            return order;
        } catch (RuntimeException ex) {
            if (!(ex instanceof PaymentFailedException)) {
                inventory.restock(cart.quantitiesByProductId());
                cart.reopenAfterFailedCheckout();
            }
            throw ex;
        }
    }

    public Order updateOrderStatus(String orderId, OrderStatus next) {
        Order order = findOrder(orderId);
        OrderStatus oldStatus = order.getStatus();
        order.transitionTo(next);
        if (next == OrderStatus.CANCELLED) {
            inventory.restock(quantitiesFrom(order));
        }
        orderRepository.save(order);
        notifyStatusChanged(order, oldStatus, next);
        return order;
    }

    public Order cancelOrder(String orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    public Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Unknown order: " + orderId));
    }

    public Inventory inventory() {
        return inventory;
    }

    private Order buildPlacedOrder(Cart cart) {
        Order.Builder builder = Order.builder(idGenerator.get(), cart.getCustomerId())
                .createdAt(clock.instant())
                .totalCents(pricingStrategy.totalCents(cart.items()));
        for (CartItem item : cart.items()) {
            builder.addItem(new OrderItem(item.product().id(), item.product().name(), item.quantity(),
                    item.product().priceCents()));
        }
        return builder.build();
    }

    private java.util.Map<String, Integer> quantitiesFrom(Order order) {
        java.util.Map<String, Integer> quantities = new java.util.LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            quantities.put(item.productId(), item.quantity());
        }
        return quantities;
    }

    private void notifyStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // EXTENSIBILITY: email, SMS, analytics, and warehouse adapters can subscribe here later.
        listeners.forEach(listener -> listener.onStatusChanged(order, oldStatus, newStatus));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * // TESTABILITY: Clock and id supplier are injected, so tests assert exact timestamps and ids
     * without sleeping or depending on UUID randomness.
     */
    public static final class Builder {
        private ProductRepository productRepository = new InMemoryProductRepository();
        private CartRepository cartRepository = new InMemoryCartRepository();
        private OrderRepository orderRepository = new InMemoryOrderRepository();
        private Inventory inventory = new Inventory();
        private PricingStrategy pricingStrategy = new PercentageDiscountTaxPricingStrategy(0, 0);
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
        private PaymentMethod paymentMethod = new AlwaysApprovedPaymentMethod(clock, idGenerator);
        private CatalogSearchStrategy searchStrategy = new KeywordCatalogSearchStrategy();
        private final List<OrderStatusListener> listeners = new ArrayList<>();

        public Builder productRepository(ProductRepository productRepository) {
            this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
            return this;
        }

        public Builder cartRepository(CartRepository cartRepository) {
            this.cartRepository = Objects.requireNonNull(cartRepository, "cartRepository");
            return this;
        }

        public Builder orderRepository(OrderRepository orderRepository) {
            this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
            return this;
        }

        public Builder inventory(Inventory inventory) {
            this.inventory = Objects.requireNonNull(inventory, "inventory");
            return this;
        }

        public Builder pricingStrategy(PricingStrategy pricingStrategy) {
            this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod");
            return this;
        }

        public Builder searchStrategy(CatalogSearchStrategy searchStrategy) {
            this.searchStrategy = Objects.requireNonNull(searchStrategy, "searchStrategy");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            this.paymentMethod = new AlwaysApprovedPaymentMethod(this.clock, this.idGenerator);
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
            this.paymentMethod = new AlwaysApprovedPaymentMethod(this.clock, this.idGenerator);
            return this;
        }

        public Builder addOrderStatusListener(OrderStatusListener listener) {
            this.listeners.add(Objects.requireNonNull(listener, "listener"));
            return this;
        }

        public ShoppingService build() {
            return new ShoppingService(this);
        }
    }
}
