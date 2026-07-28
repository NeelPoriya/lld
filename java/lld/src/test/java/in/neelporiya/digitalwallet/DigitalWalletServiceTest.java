package in.neelporiya.digitalwallet;

import in.neelporiya.digitalwallet.exception.CurrencyMismatchException;
import in.neelporiya.digitalwallet.exception.InsufficientFundsException;
import in.neelporiya.digitalwallet.exception.WalletNotFoundException;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DigitalWalletServiceTest {

    private WalletService service;
    private Wallet alice;
    private Wallet bob;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        service = new WalletService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());
        alice = service.createWallet("alice", Currency.USD);
        bob = service.createWallet("bob", Currency.USD);
    }

    private long balance(Wallet w) {
        return service.getBalance(w.getId()).minorUnits();
    }

    @Test
    void newWalletStartsAtZero() {
        assertEquals(0, balance(alice));
    }

    @Test
    void creditAndDebitAdjustBalance() {
        service.credit(alice.getId(), Money.of(10_000, Currency.USD), "k1");
        assertEquals(10_000, balance(alice));
        service.debit(alice.getId(), Money.of(2_500, Currency.USD), "k2");
        assertEquals(7_500, balance(alice));
    }

    @Test
    void debitBeyondBalanceIsRejectedAndLeavesBalanceUntouched() {
        service.credit(alice.getId(), Money.of(1_000, Currency.USD), "k1");
        assertThrows(InsufficientFundsException.class,
                () -> service.debit(alice.getId(), Money.of(5_000, Currency.USD), "k2"));
        assertEquals(1_000, balance(alice));
    }

    @Test
    void transferMovesFundsAtomically() {
        service.credit(alice.getId(), Money.of(10_000, Currency.USD), "k1");
        TransferReceipt receipt = service.transfer(alice.getId(), bob.getId(), Money.of(4_000, Currency.USD), "t1");

        assertEquals(6_000, balance(alice));
        assertEquals(4_000, balance(bob));
        assertEquals(TransactionType.TRANSFER_OUT, receipt.out().type());
        assertEquals(TransactionType.TRANSFER_IN, receipt.in().type());
    }

    @Test
    void transferWithInsufficientFundsLeavesBothBalancesUnchanged() {
        service.credit(alice.getId(), Money.of(1_000, Currency.USD), "k1");
        assertThrows(InsufficientFundsException.class,
                () -> service.transfer(alice.getId(), bob.getId(), Money.of(5_000, Currency.USD), "t1"));
        assertEquals(1_000, balance(alice));
        assertEquals(0, balance(bob));
    }

    @Test
    void currencyMismatchIsRejected() {
        assertThrows(CurrencyMismatchException.class,
                () -> service.credit(alice.getId(), Money.of(100, Currency.INR), "k1"));
    }

    @Test
    void creditIsIdempotentPerKey() {
        WalletTransaction first = service.credit(alice.getId(), Money.of(500, Currency.USD), "same-key");
        WalletTransaction second = service.credit(alice.getId(), Money.of(500, Currency.USD), "same-key");

        assertEquals(500, balance(alice), "the credit must apply exactly once");
        assertEquals(first.id(), second.id(), "retry returns the memoized transaction");
    }

    @Test
    void transferIsIdempotentPerKey() {
        service.credit(alice.getId(), Money.of(1_000, Currency.USD), "k1");
        service.transfer(alice.getId(), bob.getId(), Money.of(300, Currency.USD), "txn");
        service.transfer(alice.getId(), bob.getId(), Money.of(300, Currency.USD), "txn"); // retry

        assertEquals(700, balance(alice));
        assertEquals(300, balance(bob));
    }

    @Test
    void ledgerRecordsEveryMovement() {
        service.credit(alice.getId(), Money.of(1_000, Currency.USD), "k1");
        service.debit(alice.getId(), Money.of(200, Currency.USD), "k2");
        assertEquals(2, service.getHistory(alice.getId()).size());
    }

    @Test
    void transferToSameWalletRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(alice.getId(), alice.getId(), Money.of(1, Currency.USD), "t"));
    }

    @Test
    void unknownWalletThrows() {
        assertThrows(WalletNotFoundException.class, () -> service.getBalance("nope"));
    }
}
