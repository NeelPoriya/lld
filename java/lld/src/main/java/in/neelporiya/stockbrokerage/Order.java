package in.neelporiya.stockbrokerage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * // DESIGN PATTERN: Command — a trade instruction as a first-class object carrying its side, type,
 * quantity and (for limit orders) price, plus the runtime state the brokerage mutates as it executes.
 *
 * <p>Runtime fields ({@code status}, {@code filledPrice}, {@code filledAt}) are written only by the
 * {@link BrokerageService} while holding the owning account's lock.
 */
public class Order {

    private final String id;
    private final String accountId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final int quantity;
    private final BigDecimal limitPrice; // null for MARKET
    private final Instant createdAt;

    private volatile OrderStatus status = OrderStatus.OPEN;
    private volatile BigDecimal filledPrice;
    private volatile Instant filledAt;

    private Order(Builder b) {
        this.id = b.id;
        this.accountId = b.accountId;
        this.symbol = b.symbol;
        this.side = b.side;
        this.type = b.type;
        this.quantity = b.quantity;
        this.limitPrice = b.limitPrice;
        this.createdAt = b.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
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

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getFilledPrice() {
        return filledPrice;
    }

    public Instant getFilledAt() {
        return filledAt;
    }

    void markFilled(BigDecimal price, Instant when) {
        this.filledPrice = price;
        this.filledAt = when;
        this.status = OrderStatus.FILLED;
    }

    void markStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String accountId;
        private String symbol;
        private OrderSide side;
        private OrderType type;
        private int quantity;
        private BigDecimal limitPrice;
        private Instant createdAt = Instant.EPOCH;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder side(OrderSide side) {
            this.side = side;
            return this;
        }

        public Builder type(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder limitPrice(BigDecimal limitPrice) {
            this.limitPrice = limitPrice;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(symbol, "symbol");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            if (type == OrderType.LIMIT) {
                if (limitPrice == null || limitPrice.signum() <= 0) {
                    throw new IllegalArgumentException("limit order needs a positive limit price");
                }
            } else if (limitPrice != null) {
                throw new IllegalArgumentException("market order must not carry a limit price");
            }
            return new Order(this);
        }
    }
}
