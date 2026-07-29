package in.neelporiya.stocktrading;

/**
 * A resting or incoming order.
 *
 * <p>// INTERVIEW INSIGHT: price is a {@code long} in minor units (paise/cents). Integer prices make
 * price-time priority comparisons exact and cheap — no {@code BigDecimal} rounding inside the hot
 * matching loop. MARKET orders carry no price (0) and are never rested.
 *
 * <p>{@code remainingQuantity} and {@code status} are mutated by the {@link OrderBook} while the
 * book's lock is held.
 */
public class Order {

    private final String id;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final long price;
    private final int originalQuantity;

    private int remainingQuantity;
    private OrderStatus status = OrderStatus.NEW;

    private Order(String id, String symbol, OrderSide side, OrderType type, long price, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
    }

    public static Order limit(String id, String symbol, OrderSide side, long price, int quantity) {
        if (price <= 0) {
            throw new IllegalArgumentException("limit price must be positive");
        }
        return new Order(id, symbol, side, OrderType.LIMIT, price, quantity);
    }

    public static Order market(String id, String symbol, OrderSide side, int quantity) {
        return new Order(id, symbol, side, OrderType.MARKET, 0L, quantity);
    }

    void fill(int quantity) {
        this.remainingQuantity -= quantity;
    }

    void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public long getPrice() {
        return price;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getFilledQuantity() {
        return originalQuantity - remainingQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
