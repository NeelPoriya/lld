package in.neelporiya.restaurant;

import in.neelporiya.restaurant.billing.BillCalculator;
import in.neelporiya.restaurant.event.OrderEventListener;
import in.neelporiya.restaurant.exception.OrderNotFoundException;
import in.neelporiya.restaurant.exception.TableNotFoundException;
import in.neelporiya.restaurant.repository.MenuRepository;
import in.neelporiya.restaurant.repository.OrderRepository;
import in.neelporiya.restaurant.repository.ReservationRepository;
import in.neelporiya.restaurant.repository.TableRepository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one entry point over tables, menu, orders, billing, reservations and observers.
 *
 * <p>// TESTABILITY: {@link Clock} and id {@link Supplier}s are injected. Tests use MutableClock and
 * deterministic ids; production can wire {@code Clock.systemUTC()} and UUID suppliers.
 */
public class RestaurantService {
    private final TableRepository tableRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final BillCalculator billCalculator;
    private final List<OrderEventListener> listeners;
    private final Clock clock;
    private final Supplier<String> orderIdGenerator;
    private final Supplier<String> reservationIdGenerator;

    private RestaurantService(Builder builder) {
        this.tableRepository = builder.tableRepository;
        this.menuRepository = builder.menuRepository;
        this.orderRepository = builder.orderRepository;
        this.reservationRepository = builder.reservationRepository;
        this.billCalculator = builder.billCalculator;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
        this.clock = builder.clock;
        this.orderIdGenerator = builder.orderIdGenerator;
        this.reservationIdGenerator = builder.reservationIdGenerator;
    }

    public void addTable(Table table) {
        tableRepository.save(Objects.requireNonNull(table, "table"));
    }

    public void addMenuItem(MenuItem item) {
        menuRepository.menu().addItem(Objects.requireNonNull(item, "item"));
    }

    public MenuItem menuItem(String itemId) {
        return menuRepository.menu().requireItem(itemId);
    }

    public Order placeOrder(String tableId, List<OrderRequestItem> requestedItems) {
        Table table = requireTable(tableId);
        Order.Builder orderBuilder = Order.builder()
                .id(orderIdGenerator.get())
                .table(table)
                .placedAt(clock.instant());
        for (OrderRequestItem request : requestedItems) {
            orderBuilder.addItem(menuItem(request.menuItemId()), request.quantity());
        }
        Order order = orderBuilder.build();
        orderRepository.save(order);
        listeners.forEach(listener -> listener.onOrderPlaced(order));
        return order;
    }

    public Order changeOrderStatus(String orderId, OrderStatus next) {
        Order order = requireOrder(orderId);
        OrderStatus previous = order.changeStatus(next, clock);
        listeners.forEach(listener -> listener.onOrderStatusChanged(order, previous, next));
        return order;
    }

    public Bill bill(String orderId) {
        return billCalculator.calculate(requireOrder(orderId));
    }

    public Reservation reserveTable(String tableId, String guestName, TimeSlot slot) {
        Table table = requireTable(tableId);
        Reservation reservation = new Reservation(
                reservationIdGenerator.get(),
                table,
                guestName,
                slot,
                clock.instant());
        reservationRepository.saveIfAvailable(reservation);
        return reservation;
    }

    public Order getOrder(String orderId) {
        return requireOrder(orderId);
    }

    public List<Reservation> reservationsForTable(String tableId) {
        return reservationRepository.findByTable(tableId);
    }

    private Table requireTable(String tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException("No table with id " + tableId));
    }

    private Order requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("No order with id " + orderId));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * // DESIGN PATTERN: Builder — keeps the facade configurable without a huge constructor.
     * // EXTENSIBILITY: repositories and strategies can be replaced with database-backed or custom versions.
     */
    public static final class Builder {
        private TableRepository tableRepository = new TableRepository();
        private MenuRepository menuRepository = new MenuRepository();
        private OrderRepository orderRepository = new OrderRepository();
        private ReservationRepository reservationRepository = new ReservationRepository();
        private BillCalculator billCalculator = BillCalculator.defaults();
        private final List<OrderEventListener> listeners = new ArrayList<>();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> orderIdGenerator = () -> UUID.randomUUID().toString();
        private Supplier<String> reservationIdGenerator = () -> UUID.randomUUID().toString();

        public Builder tableRepository(TableRepository tableRepository) {
            this.tableRepository = Objects.requireNonNull(tableRepository, "tableRepository");
            return this;
        }

        public Builder menuRepository(MenuRepository menuRepository) {
            this.menuRepository = Objects.requireNonNull(menuRepository, "menuRepository");
            return this;
        }

        public Builder orderRepository(OrderRepository orderRepository) {
            this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
            return this;
        }

        public Builder reservationRepository(ReservationRepository reservationRepository) {
            this.reservationRepository = Objects.requireNonNull(reservationRepository, "reservationRepository");
            return this;
        }

        public Builder billCalculator(BillCalculator billCalculator) {
            this.billCalculator = Objects.requireNonNull(billCalculator, "billCalculator");
            return this;
        }

        public Builder addListener(OrderEventListener listener) {
            this.listeners.add(Objects.requireNonNull(listener, "listener"));
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder orderIdGenerator(Supplier<String> orderIdGenerator) {
            this.orderIdGenerator = Objects.requireNonNull(orderIdGenerator, "orderIdGenerator");
            return this;
        }

        public Builder reservationIdGenerator(Supplier<String> reservationIdGenerator) {
            this.reservationIdGenerator = Objects.requireNonNull(reservationIdGenerator, "reservationIdGenerator");
            return this;
        }

        public RestaurantService build() {
            return new RestaurantService(this);
        }
    }
}
