package in.neelporiya.stockbrokerage;

import in.neelporiya.stockbrokerage.exception.AccountNotFoundException;
import in.neelporiya.stockbrokerage.exception.InsufficientFundsException;
import in.neelporiya.stockbrokerage.exception.UnknownStockException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrokerageServiceTest {

    private InMemoryMarketData feed;
    private BrokerageService broker;

    @BeforeEach
    void setUp() {
        feed = new InMemoryMarketData().setPrice("AAPL", bd("100"));
        AtomicInteger seq = new AtomicInteger();
        broker = new BrokerageService(feed, MutableClock.atEpoch(), () -> "id" + seq.incrementAndGet());
        broker.listStock("AAPL", "Apple Inc.");
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private Account funded(String cash) {
        return broker.openAccount("alice", bd(cash));
    }

    @Test
    void marketBuyDebitsCashAndAddsSharesAtFeedPrice() {
        Account acc = funded("1000");
        Order order = broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 5);

        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertMoney("100", order.getFilledPrice());
        assertEquals(5, broker.holdingQuantity(acc.getId(), "AAPL"));
        assertMoney("500", broker.cashOf(acc.getId()));
    }

    @Test
    void marketBuyBeyondCashIsRejectedNotThrown() {
        Account acc = funded("150");
        AtomicInteger rejects = new AtomicInteger();
        broker.addListener(new OrderListener() {
            @Override
            public void onRejected(Order order, String reason) {
                rejects.incrementAndGet();
            }
        });

        Order order = broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 5); // needs 500

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals(1, rejects.get());
        assertEquals(0, broker.holdingQuantity(acc.getId(), "AAPL"));
        assertMoney("150", broker.cashOf(acc.getId()), "cash untouched on reject");
    }

    private static void assertMoney(String expected, BigDecimal actual, String msg) {
        assertEquals(0, bd(expected).compareTo(actual), msg + " (was " + actual + ")");
    }

    @Test
    void marketSellReducesSharesAndCreditsCash() {
        Account acc = funded("1000");
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 5);   // cash 500, 5 shares
        feed.setPrice("AAPL", bd("120"));

        Order sell = broker.marketOrder(acc.getId(), "AAPL", OrderSide.SELL, 2); // +240

        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(3, broker.holdingQuantity(acc.getId(), "AAPL"));
        assertMoney("740", broker.cashOf(acc.getId()));
    }

    @Test
    void sellingMoreThanHeldIsRejected() {
        Account acc = funded("1000");
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 1);

        Order sell = broker.marketOrder(acc.getId(), "AAPL", OrderSide.SELL, 5);
        assertEquals(OrderStatus.REJECTED, sell.getStatus());
        assertEquals(1, broker.holdingQuantity(acc.getId(), "AAPL"));
    }

    @Test
    void marketableLimitBuyFillsImmediatelyWithPriceImprovement() {
        Account acc = funded("1000");
        // Willing to pay up to 110, market is 100 -> fills at 100.
        Order order = broker.limitOrder(acc.getId(), "AAPL", OrderSide.BUY, 3, bd("110"));

        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertMoney("100", order.getFilledPrice());
        assertMoney("700", broker.cashOf(acc.getId()));
    }

    @Test
    void nonMarketableLimitBuyRestsThenFillsWhenPriceDrops() {
        Account acc = funded("1000");
        Order order = broker.limitOrder(acc.getId(), "AAPL", OrderSide.BUY, 4, bd("90"));
        assertEquals(OrderStatus.OPEN, order.getStatus(), "market 100 > limit 90 -> rests");

        assertEquals(0, broker.notifyPriceChanged("AAPL"), "still 100, nothing fills");

        feed.setPrice("AAPL", bd("90"));
        assertEquals(1, broker.notifyPriceChanged("AAPL"));
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertMoney("90", order.getFilledPrice());
        assertEquals(4, broker.holdingQuantity(acc.getId(), "AAPL"));
    }

    @Test
    void nonMarketableLimitSellRestsThenFillsWhenPriceRises() {
        Account acc = funded("1000");
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 5); // 5 shares, cash 500
        Order sell = broker.limitOrder(acc.getId(), "AAPL", OrderSide.SELL, 5, bd("130"));
        assertEquals(OrderStatus.OPEN, sell.getStatus());

        feed.setPrice("AAPL", bd("130"));
        assertEquals(1, broker.notifyPriceChanged("AAPL"));
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertMoney("1150", broker.cashOf(acc.getId())); // 500 + 5*130
        assertEquals(0, broker.holdingQuantity(acc.getId(), "AAPL"));
    }

    @Test
    void cancelOpenLimitOrderThenPriceMoveDoesNothing() {
        Account acc = funded("1000");
        Order order = broker.limitOrder(acc.getId(), "AAPL", OrderSide.BUY, 4, bd("90"));

        assertTrue(broker.cancelOrder(order.getId()));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        feed.setPrice("AAPL", bd("90"));
        assertEquals(0, broker.notifyPriceChanged("AAPL"), "cancelled order must not fill");
        assertEquals(0, broker.holdingQuantity(acc.getId(), "AAPL"));
    }

    @Test
    void cancellingAFilledOrderReturnsFalse() {
        Account acc = funded("1000");
        Order order = broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 1);
        assertEquals(OrderStatus.FILLED, order.getStatus());
        assertFalse(broker.cancelOrder(order.getId()));
    }

    @Test
    void averageCostBasisBlendsAcrossBuys() {
        Account acc = funded("10000");
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 10); // 10 @ 100
        feed.setPrice("AAPL", bd("120"));
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 10); // 10 @ 120

        Holding holding = acc.getPortfolio().getHoldings().get("AAPL");
        assertEquals(20, holding.getQuantity());
        assertMoney("110", holding.getAverageCost()); // (10*100 + 10*120)/20
    }

    @Test
    void portfolioValueMarksHoldingsToMarket() {
        Account acc = funded("1000");
        broker.marketOrder(acc.getId(), "AAPL", OrderSide.BUY, 5); // cash 500 + 5 shares
        feed.setPrice("AAPL", bd("130"));

        // 500 cash + 5 * 130 = 1150
        assertMoney("1150", broker.portfolioValue(acc.getId()));
    }

    @Test
    void depositAndWithdraw() {
        Account acc = funded("100");
        broker.deposit(acc.getId(), bd("50"));
        assertMoney("150", broker.cashOf(acc.getId()));
        broker.withdraw(acc.getId(), bd("120"));
        assertMoney("30", broker.cashOf(acc.getId()));
        assertThrows(InsufficientFundsException.class, () -> broker.withdraw(acc.getId(), bd("100")));
    }

    @Test
    void structuralErrorsThrow() {
        assertThrows(AccountNotFoundException.class,
                () -> broker.marketOrder("ghost", "AAPL", OrderSide.BUY, 1));
        Account acc = funded("1000");
        assertThrows(UnknownStockException.class,
                () -> broker.marketOrder(acc.getId(), "TSLA", OrderSide.BUY, 1));
    }
}
