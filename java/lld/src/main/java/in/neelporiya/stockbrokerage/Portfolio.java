package in.neelporiya.stockbrokerage;

import in.neelporiya.stockbrokerage.exception.InsufficientFundsException;
import in.neelporiya.stockbrokerage.exception.InsufficientHoldingsException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The cash + positions of one account and the money invariants: cash never goes negative and you
 * cannot sell shares you don't own (no shorting).
 *
 * <p>// CONCURRENCY: this class is deliberately NOT internally synchronized. Every mutation is a
 * read-modify-write on cash and a holding at once, so it must run under the owning account's single
 * lock (held by {@link BrokerageService}). Internal locking here would give a false sense of safety
 * because the invariant spans two fields.
 */
public class Portfolio {

    private BigDecimal cash;
    private final Map<String, Holding> holdings = new LinkedHashMap<>();

    Portfolio(BigDecimal openingCash) {
        this.cash = openingCash;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public int quantityOf(String symbol) {
        Holding holding = holdings.get(symbol);
        return holding == null ? 0 : holding.getQuantity();
    }

    public Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(holdings));
    }

    void deposit(BigDecimal amount) {
        requirePositive(amount);
        cash = cash.add(amount);
    }

    void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (cash.compareTo(amount) < 0) {
            throw new InsufficientFundsException("cash " + cash + " < withdrawal " + amount);
        }
        cash = cash.subtract(amount);
    }

    /** Debit cash and grow the position, updating the average cost basis. */
    void buy(String symbol, int quantity, BigDecimal price) {
        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));
        if (cash.compareTo(cost) < 0) {
            throw new InsufficientFundsException("cash " + cash + " < cost " + cost);
        }
        cash = cash.subtract(cost);
        Holding holding = holdings.get(symbol);
        if (holding == null) {
            holdings.put(symbol, new Holding(symbol, quantity, price));
            return;
        }
        int newQty = holding.getQuantity() + quantity;
        BigDecimal oldValue = holding.getAverageCost().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal newAverage = oldValue.add(cost).divide(BigDecimal.valueOf(newQty), 10, RoundingMode.HALF_UP);
        holding.setQuantity(newQty);
        holding.setAverageCost(newAverage);
    }

    /** Reduce the position and credit cash; average cost is unchanged when trimming a position. */
    void sell(String symbol, int quantity, BigDecimal price) {
        Holding holding = holdings.get(symbol);
        int held = holding == null ? 0 : holding.getQuantity();
        if (held < quantity) {
            throw new InsufficientHoldingsException("hold " + held + " of " + symbol + " < sell " + quantity);
        }
        cash = cash.add(price.multiply(BigDecimal.valueOf(quantity)));
        int remaining = held - quantity;
        if (remaining == 0) {
            holdings.remove(symbol);
        } else {
            holding.setQuantity(remaining);
        }
    }

    /** Cash plus every position marked to the current market price. */
    BigDecimal marketValue(MarketDataProvider marketData) {
        BigDecimal total = cash;
        for (Holding holding : holdings.values()) {
            total = total.add(marketData.priceOf(holding.getSymbol())
                    .multiply(BigDecimal.valueOf(holding.getQuantity())));
        }
        return total;
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
