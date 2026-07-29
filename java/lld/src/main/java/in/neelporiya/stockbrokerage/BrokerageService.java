package in.neelporiya.stockbrokerage;

import in.neelporiya.stockbrokerage.exception.AccountNotFoundException;
import in.neelporiya.stockbrokerage.exception.InsufficientFundsException;
import in.neelporiya.stockbrokerage.exception.InsufficientHoldingsException;
import in.neelporiya.stockbrokerage.exception.UnknownStockException;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one API over accounts, cash, a stock catalog and order execution.
 *
 * <p>Execution model (this is a BROKERAGE, not an exchange): orders are priced off an injected
 * {@link MarketDataProvider}. A market order (or a limit order already at/through its price) fills
 * immediately; a limit order that isn't marketable yet REST as {@link OrderStatus#OPEN} and is
 * re-checked when {@link #notifyPriceChanged(String)} reports the market has moved. The order-book
 * matching that pairs one client's buy with another's sell is the separate Stock Trading System.
 *
 * <p>// INTERVIEW INSIGHT: funding outcomes are modelled as ORDER STATE (FILLED / OPEN / REJECTED)
 * plus observer callbacks, not thrown exceptions — because a resting order can be rejected long after
 * {@code placeOrder} returned. Exceptions are reserved for structural errors (unknown account/stock).
 */
public class BrokerageService {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Stock> catalog = new ConcurrentHashMap<>();
    private final Map<String, Order> ordersById = new ConcurrentHashMap<>();
    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();

    private final MarketDataProvider marketData;
    private final Clock clock;
    private final Supplier<String> idGenerator;

    public BrokerageService(MarketDataProvider marketData, Clock clock) {
        this(marketData, clock, () -> UUID.randomUUID().toString());
    }

    public BrokerageService(MarketDataProvider marketData, Clock clock, Supplier<String> idGenerator) {
        this.marketData = Objects.requireNonNull(marketData, "marketData");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    public Stock listStock(String symbol, String companyName) {
        Stock stock = new Stock(symbol, companyName);
        catalog.put(symbol, stock);
        return stock;
    }

    public Account openAccount(String ownerName, BigDecimal openingCash) {
        if (openingCash == null || openingCash.signum() < 0) {
            throw new IllegalArgumentException("opening cash cannot be negative");
        }
        Account account = new Account(idGenerator.get(), ownerName, new Portfolio(openingCash));
        accounts.put(account.getId(), account);
        return account;
    }

    public void deposit(String accountId, BigDecimal amount) {
        Account account = requireAccount(accountId);
        account.getLock().lock();
        try {
            account.getPortfolio().deposit(amount);
        } finally {
            account.getLock().unlock();
        }
    }

    /** @throws InsufficientFundsException if the account lacks the cash. */
    public void withdraw(String accountId, BigDecimal amount) {
        Account account = requireAccount(accountId);
        account.getLock().lock();
        try {
            account.getPortfolio().withdraw(amount);
        } finally {
            account.getLock().unlock();
        }
    }

    public Order marketOrder(String accountId, String symbol, OrderSide side, int quantity) {
        return placeOrder(accountId, symbol, side, OrderType.MARKET, quantity, null);
    }

    public Order limitOrder(String accountId, String symbol, OrderSide side, int quantity, BigDecimal limit) {
        return placeOrder(accountId, symbol, side, OrderType.LIMIT, quantity, limit);
    }

    public Order placeOrder(String accountId, String symbol, OrderSide side, OrderType type,
                            int quantity, BigDecimal limitPrice) {
        Account account = requireAccount(accountId);
        requireStock(symbol);
        Order order = Order.builder()
                .id(idGenerator.get())
                .accountId(accountId)
                .symbol(symbol)
                .side(side)
                .type(type)
                .quantity(quantity)
                .limitPrice(limitPrice)
                .createdAt(clock.instant())
                .build();
        ordersById.put(order.getId(), order);
        account.addOrder(order);

        BigDecimal marketPrice = marketData.priceOf(symbol);
        account.getLock().lock();
        try {
            if (type == OrderType.MARKET || isMarketable(side, marketPrice, limitPrice)) {
                execute(account, order, marketPrice);
            }
            // else: a non-marketable limit order simply rests as OPEN.
        } finally {
            account.getLock().unlock();
        }
        return order;
    }

    public boolean cancelOrder(String orderId) {
        Order order = ordersById.get(orderId);
        if (order == null) {
            return false;
        }
        Account account = accounts.get(order.getAccountId());
        account.getLock().lock();
        try {
            if (order.getStatus() != OrderStatus.OPEN) {
                return false;
            }
            order.markStatus(OrderStatus.CANCELLED);
            listeners.forEach(l -> l.onCancelled(order));
            return true;
        } finally {
            account.getLock().unlock();
        }
    }

    /**
     * Report that {@code symbol} has a new market price; fill any resting limit order that has now
     * become marketable.
     *
     * @return how many resting orders filled.
     */
    public int notifyPriceChanged(String symbol) {
        BigDecimal price = marketData.priceOf(symbol);
        int filled = 0;
        for (Order order : ordersById.values()) {
            if (order.getType() != OrderType.LIMIT || !order.getSymbol().equals(symbol)
                    || order.getStatus() != OrderStatus.OPEN) {
                continue;
            }
            Account account = accounts.get(order.getAccountId());
            account.getLock().lock();
            try {
                // Re-check under the lock: it may have been cancelled or filled meanwhile.
                if (order.getStatus() == OrderStatus.OPEN && isMarketable(order.getSide(), price, order.getLimitPrice())) {
                    execute(account, order, price);
                    if (order.getStatus() == OrderStatus.FILLED) {
                        filled++;
                    }
                }
            } finally {
                account.getLock().unlock();
            }
        }
        return filled;
    }

    public BigDecimal portfolioValue(String accountId) {
        Account account = requireAccount(accountId);
        account.getLock().lock();
        try {
            return account.getPortfolio().marketValue(marketData);
        } finally {
            account.getLock().unlock();
        }
    }

    public BigDecimal cashOf(String accountId) {
        Account account = requireAccount(accountId);
        account.getLock().lock();
        try {
            return account.getPortfolio().getCash();
        } finally {
            account.getLock().unlock();
        }
    }

    public int holdingQuantity(String accountId, String symbol) {
        Account account = requireAccount(accountId);
        account.getLock().lock();
        try {
            return account.getPortfolio().quantityOf(symbol);
        } finally {
            account.getLock().unlock();
        }
    }

    public Order getOrder(String orderId) {
        return ordersById.get(orderId);
    }

    // --- internals (caller must hold account.lock) ---

    private void execute(Account account, Order order, BigDecimal price) {
        Portfolio portfolio = account.getPortfolio();
        try {
            if (order.getSide() == OrderSide.BUY) {
                portfolio.buy(order.getSymbol(), order.getQuantity(), price);
            } else {
                portfolio.sell(order.getSymbol(), order.getQuantity(), price);
            }
            order.markFilled(price, clock.instant());
            listeners.forEach(l -> l.onFilled(order));
        } catch (InsufficientFundsException | InsufficientHoldingsException failure) {
            order.markStatus(OrderStatus.REJECTED);
            listeners.forEach(l -> l.onRejected(order, failure.getMessage()));
        }
    }

    private static boolean isMarketable(OrderSide side, BigDecimal marketPrice, BigDecimal limitPrice) {
        return side == OrderSide.BUY
                ? marketPrice.compareTo(limitPrice) <= 0   // buy at limit or lower
                : marketPrice.compareTo(limitPrice) >= 0;  // sell at limit or higher
    }

    private Account requireAccount(String accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException("no account " + accountId);
        }
        return account;
    }

    private void requireStock(String symbol) {
        if (!catalog.containsKey(symbol)) {
            throw new UnknownStockException("stock not listed: " + symbol);
        }
    }
}
