package in.neelporiya.vendingmachine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendingMachineTest {

    private VendingMachine.Builder baseBuilder() {
        return VendingMachine.builder()
                .addChange(Coin.QUARTER, 10)
                .addChange(Coin.DIME, 10)
                .addChange(Coin.NICKEL, 10)
                .addChange(Coin.PENNY, 10);
    }

    @Test
    void exactMoneyDispensesProductAndCollectsBalance() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 65, 2)
                .build();

        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.DIME);
        machine.insertCoin(Coin.NICKEL);
        DispenseResult result = machine.selectProduct("A1");

        assertEquals("Cola", result.product().name());
        assertEquals(0, result.changeCents());
        assertEquals(65, result.amountPaidCents());
        assertEquals(65, machine.getCollectedBalanceCents());
        assertEquals(1, machine.getQuantity("A1"));
        assertEquals("IDLE", machine.getStateName());
    }

    @Test
    void returnsCorrectChangeUsingGreedyStrategy() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 65, 3)
                .build();

        machine.insertNote(Note.ONE_DOLLAR);
        DispenseResult result = machine.selectProduct("A1");

        assertEquals(35, result.changeCents());
        assertEquals(List.of(Coin.QUARTER, Coin.DIME), result.change());
        assertEquals(65, machine.getCollectedBalanceCents());
        assertEquals("IDLE", machine.getStateName());
    }

    @Test
    void insufficientFundsDoesNotDispenseAndKeepsMoneyInserted() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 65, 1)
                .build();

        machine.insertCoin(Coin.QUARTER);

        assertThrows(InsufficientFundsException.class, () -> machine.selectProduct("A1"));
        assertEquals(1, machine.getQuantity("A1"));
        assertEquals(25, machine.getInsertedBalanceCents());
        assertEquals("HAS_MONEY", machine.getStateName());
    }

    @Test
    void outOfStockProductIsRejectedWithoutLosingInsertedMoney() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 65, 0)
                .addProduct("B1", "Chips", 50, 1)
                .build();

        machine.insertNote(Note.ONE_DOLLAR);

        assertThrows(OutOfStockException.class, () -> machine.selectProduct("A1"));
        assertEquals(100, machine.getInsertedBalanceCents());
        assertEquals("HAS_MONEY", machine.getStateName());
    }

    @Test
    void refundReturnsAllInsertedMoneyAndGoesIdle() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 65, 1)
                .build();

        machine.insertCoin(Coin.QUARTER);
        machine.insertNote(Note.ONE_DOLLAR);
        RefundResult refund = machine.refund();

        assertEquals(List.of(Coin.QUARTER, Note.ONE_DOLLAR), refund.returnedMoney());
        assertEquals(125, refund.totalCents());
        assertEquals(0, machine.getInsertedBalanceCents());
        assertEquals("IDLE", machine.getStateName());
    }

    @Test
    void cannotMakeExactChangeRejectsPurchaseWithoutChangingInventoryOrBalance() {
        VendingMachine machine = VendingMachine.builder()
                .addProduct("A1", "Cola", 65, 1)
                .build();

        machine.insertNote(Note.ONE_DOLLAR);

        assertThrows(UnableToMakeChangeException.class, () -> machine.selectProduct("A1"));
        assertEquals(1, machine.getQuantity("A1"));
        assertEquals(0, machine.getCollectedBalanceCents());
        assertEquals(100, machine.getInsertedBalanceCents());
        assertEquals("HAS_MONEY", machine.getStateName());
    }

    @Test
    void stateTransitionsThroughIdleHasMoneyAndOutOfStock() {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Water", 100, 1)
                .build();

        assertEquals("IDLE", machine.getStateName());
        machine.insertNote(Note.ONE_DOLLAR);
        assertEquals("HAS_MONEY", machine.getStateName());
        DispenseResult result = machine.selectProduct("A1");

        assertEquals("Water", result.product().name());
        assertEquals("OUT_OF_STOCK", machine.getStateName());
        assertThrows(OutOfStockException.class, () -> machine.insertCoin(Coin.QUARTER));
    }

    @Test
    void concurrentSelectionsCannotOversellLastUnit() throws Exception {
        VendingMachine machine = baseBuilder()
                .addProduct("A1", "Cola", 100, 1)
                .build();
        machine.insertNote(Note.ONE_DOLLAR);

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        List<Throwable> failures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    machine.selectProduct("A1");
                    successes.incrementAndGet();
                } catch (InvalidStateException | OutOfStockException ignored) {
                    // Expected for losing threads: the only funded transaction already completed.
                } catch (Throwable t) {
                    synchronized (failures) {
                        failures.add(t);
                    }
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(failures.isEmpty(), () -> "Unexpected failure: " + failures);
        assertEquals(1, successes.get());
        assertEquals(0, machine.getQuantity("A1"));
        assertEquals(100, machine.getCollectedBalanceCents());
    }
}
