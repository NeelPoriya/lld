package in.neelporiya.fooddelivery;

import in.neelporiya.fooddelivery.exception.EmptyCartException;
import in.neelporiya.fooddelivery.exception.InvalidOrderTransitionException;
import in.neelporiya.fooddelivery.exception.ItemUnavailableException;
import in.neelporiya.fooddelivery.exception.NoAgentAvailableException;
import in.neelporiya.fooddelivery.exception.NotFoundException;
import in.neelporiya.fooddelivery.exception.RestaurantClosedException;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — the whole ordering flow: onboard restaurants/agents/customers, search,
 * place an order, drive it through its lifecycle, and dispatch a courier.
 *
 * <p>Two strategies are injected: {@link PricingStrategy} (cart + distance → bill) and
 * {@link AgentAssignmentStrategy} (rank free couriers). A {@link Clock} and id generator make it
 * deterministic.
 */
public class FoodDeliveryService {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Restaurant> restaurantsById = new ConcurrentHashMap<>();
    private final List<Restaurant> restaurants = new CopyOnWriteArrayList<>();
    private final List<DeliveryAgent> agents = new CopyOnWriteArrayList<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();

    private final PricingStrategy pricingStrategy;
    private final AgentAssignmentStrategy agentStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public FoodDeliveryService(PricingStrategy pricingStrategy, AgentAssignmentStrategy agentStrategy, Clock clock) {
        this(pricingStrategy, agentStrategy, clock, () -> UUID.randomUUID().toString());
    }

    public FoodDeliveryService(PricingStrategy pricingStrategy, AgentAssignmentStrategy agentStrategy,
                               Clock clock, Supplier<String> idGenerator) {
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
        this.agentStrategy = Objects.requireNonNull(agentStrategy, "agentStrategy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    // --- onboarding ---

    public Customer registerCustomer(String name, Location location) {
        Customer customer = new Customer(idGenerator.get(), name, location);
        customers.put(customer.id(), customer);
        return customer;
    }

    public Restaurant registerRestaurant(String name, Location location) {
        Restaurant restaurant = new Restaurant(idGenerator.get(), name, location);
        restaurantsById.put(restaurant.getId(), restaurant);
        restaurants.add(restaurant);
        return restaurant;
    }

    public DeliveryAgent registerAgent(String name, Location location) {
        DeliveryAgent agent = new DeliveryAgent(idGenerator.get(), name, location);
        agents.add(agent);
        return agent;
    }

    public Cart newCart(String restaurantId) {
        requireRestaurant(restaurantId);
        return new Cart(restaurantId);
    }

    // --- discovery ---

    /** Open restaurants whose name or any menu item name contains {@code query} (case-insensitive). */
    public List<Restaurant> searchRestaurants(String query) {
        String needle = query.toLowerCase(Locale.ROOT);
        return restaurants.stream()
                .filter(Restaurant::isOpen)
                .filter(r -> r.getName().toLowerCase(Locale.ROOT).contains(needle)
                        || r.getMenu().items().stream()
                        .anyMatch(i -> i.name().toLowerCase(Locale.ROOT).contains(needle)))
                .toList();
    }

    // --- ordering ---

    public Order placeOrder(String customerId, Cart cart) {
        Customer customer = requireCustomer(customerId);
        Restaurant restaurant = requireRestaurant(cart.getRestaurantId());
        if (!restaurant.isOpen()) {
            throw new RestaurantClosedException(restaurant.getName() + " is closed");
        }
        if (cart.isEmpty()) {
            throw new EmptyCartException("cannot place an empty order");
        }
        List<OrderLine> lines = cart.toOrderLines();
        for (OrderLine line : lines) {
            if (!restaurant.getMenu().isAvailable(line.item().id())) {
                throw new ItemUnavailableException(line.item().name() + " is unavailable");
            }
        }
        double distance = restaurant.getLocation().distanceTo(customer.location());
        Bill bill = pricingStrategy.price(cart, distance);
        Order order = new Order(idGenerator.get(), customer, restaurant, lines, bill, clock.instant());
        orders.put(order.getId(), order);
        listeners.forEach(l -> l.onPlaced(order));
        return order;
    }

    public void acceptOrder(String orderId) {
        transition(requireOrder(orderId), OrderStatus.ACCEPTED);
    }

    public void startPreparing(String orderId) {
        transition(requireOrder(orderId), OrderStatus.PREPARING);
    }

    public void markReady(String orderId) {
        transition(requireOrder(orderId), OrderStatus.READY_FOR_PICKUP);
    }

    /**
     * Assign a courier and send the order out. Claims an agent atomically per the strategy's ranking.
     *
     * @throws NoAgentAvailableException if every courier is busy (status is left unchanged).
     */
    public DeliveryAgent dispatch(String orderId) {
        Order order = requireOrder(orderId);
        order.getLock().lock();
        try {
            if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
                throw new InvalidOrderTransitionException(
                        "dispatch needs READY_FOR_PICKUP but was " + order.getStatus());
            }
            DeliveryAgent agent = claimAgent(order.getRestaurant().getLocation());
            order.assignAgent(agent);
            order.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
            listeners.forEach(l -> l.onAgentAssigned(order, agent));
            listeners.forEach(l -> l.onStatusChanged(order, OrderStatus.READY_FOR_PICKUP, OrderStatus.OUT_FOR_DELIVERY));
            return agent;
        } finally {
            order.getLock().unlock();
        }
    }

    public void deliver(String orderId) {
        Order order = requireOrder(orderId);
        order.getLock().lock();
        try {
            OrderStatus from = order.getStatus();
            order.transitionTo(OrderStatus.DELIVERED);
            releaseAgent(order);
            listeners.forEach(l -> l.onStatusChanged(order, from, OrderStatus.DELIVERED));
        } finally {
            order.getLock().unlock();
        }
    }

    public void cancelOrder(String orderId) {
        Order order = requireOrder(orderId);
        order.getLock().lock();
        try {
            OrderStatus from = order.getStatus();
            order.transitionTo(OrderStatus.CANCELLED);
            releaseAgent(order);
            listeners.forEach(l -> l.onStatusChanged(order, from, OrderStatus.CANCELLED));
        } finally {
            order.getLock().unlock();
        }
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    // --- internals ---

    private void transition(Order order, OrderStatus to) {
        order.getLock().lock();
        try {
            OrderStatus from = order.getStatus();
            order.transitionTo(to);
            listeners.forEach(l -> l.onStatusChanged(order, from, to));
        } finally {
            order.getLock().unlock();
        }
    }

    private DeliveryAgent claimAgent(Location pickup) {
        List<DeliveryAgent> free = agents.stream().filter(DeliveryAgent::isAvailable).toList();
        for (DeliveryAgent candidate : agentStrategy.rank(free, pickup)) {
            if (candidate.tryClaim()) {
                return candidate;
            }
        }
        throw new NoAgentAvailableException("no courier available for pickup");
    }

    private static void releaseAgent(Order order) {
        DeliveryAgent agent = order.getAgent();
        if (agent != null) {
            agent.release();
        }
    }

    private Customer requireCustomer(String id) {
        Customer customer = customers.get(id);
        if (customer == null) {
            throw new NotFoundException("no customer " + id);
        }
        return customer;
    }

    private Restaurant requireRestaurant(String id) {
        Restaurant restaurant = restaurantsById.get(id);
        if (restaurant == null) {
            throw new NotFoundException("no restaurant " + id);
        }
        return restaurant;
    }

    private Order requireOrder(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new NotFoundException("no order " + id);
        }
        return order;
    }
}
