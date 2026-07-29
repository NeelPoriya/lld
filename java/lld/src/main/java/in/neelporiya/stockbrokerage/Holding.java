package in.neelporiya.stockbrokerage;

import java.math.BigDecimal;

/**
 * A position in one symbol: how many shares and the average price paid (cost basis).
 *
 * <p>Not thread-safe on its own — mutated only by {@link Portfolio} while the owning account's lock
 * is held.
 */
public class Holding {

    private final String symbol;
    private int quantity;
    private BigDecimal averageCost;

    Holding(String symbol, int quantity, BigDecimal averageCost) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }
}
