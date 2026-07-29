package in.neelporiya.stocktrading;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingEngineTest {

    private static final String SYM = "AAPL";

    private final MatchingEngine engine = new MatchingEngine();

    @Test
    void limitOrderRestsWhenThereIsNoCounterparty() {
        Order sell = Order.limit("s1", SYM, OrderSide.SELL, 101, 100);
        List<Trade> trades = engine.placeOrder(sell);

        assertTrue(trades.isEmpty());
        assertEquals(OrderStatus.NEW, sell.getStatus());
        assertEquals(101, engine.getOrderBook(SYM).bestAsk().orElseThrow());
        assertEquals(1, engine.getOrderBook(SYM).restingOrderCount());
    }

    @Test
    void crossingOrdersTradeAtTheMakerPrice() {
        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 100, 100)); // maker
        List<Trade> trades = engine.placeOrder(Order.limit("b1", SYM, OrderSide.BUY, 105, 40)); // taker

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertEquals(100, trade.price(), "executes at resting/maker price, not the aggressive 105");
        assertEquals(40, trade.quantity());
        assertEquals("b1", trade.buyOrderId());
        assertEquals("s1", trade.sellOrderId());
        assertEquals(100, engine.getOrderBook(SYM).bestAsk().orElseThrow(), "60 of the seller still rests at 100");
        assertEquals(1, engine.getOrderBook(SYM).restingOrderCount());
    }

    @Test
    void takerFillsAcrossMultiplePriceLevels() {
        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 100, 50));
        engine.placeOrder(Order.limit("s2", SYM, OrderSide.SELL, 101, 100));

        Order buy = Order.limit("b1", SYM, OrderSide.BUY, 101, 120);
        List<Trade> trades = engine.placeOrder(buy);

        assertEquals(2, trades.size());
        assertEquals(100, trades.get(0).price());
        assertEquals(50, trades.get(0).quantity());
        assertEquals(101, trades.get(1).price());
        assertEquals(70, trades.get(1).quantity());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals(101, engine.getOrderBook(SYM).bestAsk().orElseThrow());
        assertEquals(1, engine.getOrderBook(SYM).restingOrderCount(), "30 of s2 still resting");
    }

    @Test
    void priceTimePriorityIsFifoWithinALevel() {
        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 100, 10)); // earlier
        engine.placeOrder(Order.limit("s2", SYM, OrderSide.SELL, 100, 10)); // later

        List<Trade> trades = engine.placeOrder(Order.limit("b1", SYM, OrderSide.BUY, 100, 15));

        assertEquals(2, trades.size());
        assertEquals("s1", trades.get(0).sellOrderId(), "earliest resting order fills first");
        assertEquals(10, trades.get(0).quantity());
        assertEquals("s2", trades.get(1).sellOrderId());
        assertEquals(5, trades.get(1).quantity());
    }

    @Test
    void marketOrderSweepsLiquidity() {
        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 100, 30));
        Order marketBuy = Order.market("b1", SYM, OrderSide.BUY, 50);

        List<Trade> trades = engine.placeOrder(marketBuy);

        assertEquals(1, trades.size());
        assertEquals(100, trades.get(0).price());
        assertEquals(30, trades.get(0).quantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, marketBuy.getStatus(), "20 unfilled, not rested");
        assertTrue(engine.getOrderBook(SYM).bestAsk().isEmpty());
    }

    @Test
    void marketOrderWithNoLiquidityIsRejected() {
        Order marketBuy = Order.market("b1", SYM, OrderSide.BUY, 10);
        List<Trade> trades = engine.placeOrder(marketBuy);

        assertTrue(trades.isEmpty());
        assertEquals(OrderStatus.REJECTED, marketBuy.getStatus());
    }

    @Test
    void nonCrossingLimitOrdersBothRest() {
        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 101, 10)); // ask 101
        List<Trade> trades = engine.placeOrder(Order.limit("b1", SYM, OrderSide.BUY, 100, 10)); // bid 100

        assertTrue(trades.isEmpty(), "bid 100 < ask 101, no cross");
        assertEquals(100, engine.getOrderBook(SYM).bestBid().orElseThrow());
        assertEquals(101, engine.getOrderBook(SYM).bestAsk().orElseThrow());
    }

    @Test
    void incomingSellCrossesRestingBids() {
        engine.placeOrder(Order.limit("b1", SYM, OrderSide.BUY, 100, 50)); // resting bid
        Order sell = Order.limit("s1", SYM, OrderSide.SELL, 100, 30);
        List<Trade> trades = engine.placeOrder(sell);

        assertEquals(1, trades.size());
        assertEquals("b1", trades.get(0).buyOrderId());
        assertEquals("s1", trades.get(0).sellOrderId());
        assertEquals(30, trades.get(0).quantity());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(100, engine.getOrderBook(SYM).bestBid().orElseThrow(), "20 of the bid remains");
    }

    @Test
    void cancelRemovesARestingOrder() {
        Order buy = Order.limit("b1", SYM, OrderSide.BUY, 100, 10);
        engine.placeOrder(buy);
        assertEquals(1, engine.getOrderBook(SYM).restingOrderCount());

        assertTrue(engine.cancelOrder(SYM, "b1"));
        assertEquals(OrderStatus.CANCELLED, buy.getStatus());
        assertTrue(engine.getOrderBook(SYM).bestBid().isEmpty());
        assertFalse(engine.cancelOrder(SYM, "b1"), "cancelling twice is a no-op");
    }

    @Test
    void tradesArePublishedToListeners() {
        List<Trade> published = new ArrayList<>();
        engine.addListener(published::add);

        engine.placeOrder(Order.limit("s1", SYM, OrderSide.SELL, 100, 10));
        engine.placeOrder(Order.limit("b1", SYM, OrderSide.BUY, 100, 10));

        assertEquals(1, published.size());
        assertEquals(10, published.get(0).quantity());
    }
}
