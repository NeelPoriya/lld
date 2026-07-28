package in.neelporiya.atm;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtmTest {

    private static final Card CARD = new Card("CARD-1", "A-1");

    private final MutableClock clock = MutableClock.atEpoch();

    private Atm atmWith(Account account, CashInventory inventory) {
        Bank bank = new Bank().addAccount(account);
        return Atm.builder()
                .bank(bank)
                .cashInventory(inventory)
                .clock(clock)
                .build();
    }

    private Atm readyAtm(int openingBalanceRupees, CashInventory inventory) {
        return atmWith(new Account("A-1", "1234", cents(openingBalanceRupees)), inventory);
    }

    @Test
    void pinAuthenticationSucceedsAndLockoutHappensAfterMaxWrongAttempts() {
        Account account = new Account("A-1", "1234", cents(1_000), 3);
        Atm atm = atmWith(account, stockedInventory());

        atm.insertCard(CARD);
        PinAuthenticationResult wrong1 = atm.enterPin("0000");
        assertFalse(wrong1.authenticated());
        assertFalse(wrong1.locked());
        assertEquals(2, wrong1.attemptsRemaining());
        assertEquals("CARD_INSERTED", atm.getStateName());

        PinAuthenticationResult wrong2 = atm.enterPin("1111");
        assertEquals(1, wrong2.attemptsRemaining());

        PinAuthenticationResult wrong3 = atm.enterPin("2222");
        assertFalse(wrong3.authenticated());
        assertTrue(wrong3.locked());
        assertTrue(account.isLocked());
        assertEquals("IDLE", atm.getStateName());

        atm.insertCard(CARD);
        PinAuthenticationResult correctAfterLock = atm.enterPin("1234");
        assertFalse(correctAfterLock.authenticated(), "locked account must reject even the correct PIN");
        assertTrue(correctAfterLock.locked());
    }

    @Test
    void balanceInquiryReturnsCurrentBalanceAndTimestamp() {
        Atm atm = readyAtm(1_500, stockedInventory());
        authenticate(atm);
        clock.advance(Duration.ofMinutes(5));

        atm.selectOperation(AtmOperation.BALANCE_INQUIRY);
        BalanceInquiryResult result = atm.balanceInquiry();

        assertEquals("A-1", result.accountId());
        assertEquals(cents(1_500), result.balanceCents());
        assertEquals(clock.instant(), result.timestamp());
    }

    @Test
    void withdrawSuccessUsesChainOfResponsibilityNoteBreakdown() {
        CashInventory inventory = new CashInventory()
                .add(NoteDenomination.TWO_THOUSAND, 1)
                .add(NoteDenomination.FIVE_HUNDRED, 1)
                .add(NoteDenomination.TWO_HUNDRED, 1)
                .add(NoteDenomination.ONE_HUNDRED, 1);
        Atm atm = readyAtm(10_000, inventory);
        authenticate(atm);

        atm.selectOperation(AtmOperation.WITHDRAW);
        WithdrawResult result = atm.withdraw(cents(2_800));

        assertEquals(cents(7_200), result.balanceAfterCents());
        assertEquals(Map.of(
                NoteDenomination.TWO_THOUSAND, 1,
                NoteDenomination.FIVE_HUNDRED, 1,
                NoteDenomination.TWO_HUNDRED, 1,
                NoteDenomination.ONE_HUNDRED, 1), result.dispensedNotes());
        assertEquals(0, inventory.totalCents());
    }

    @Test
    void insufficientAccountFundsAreRejectedAndInventoryIsUntouched() {
        CashInventory inventory = stockedInventory();
        Atm atm = readyAtm(400, inventory);
        authenticate(atm);

        atm.selectOperation(AtmOperation.WITHDRAW);
        assertThrows(InsufficientFundsException.class, () -> atm.withdraw(cents(500)));
        assertEquals(cents(8_400), inventory.totalCents());
    }

    @Test
    void insufficientAtmCashIsRejected() {
        CashInventory inventory = new CashInventory().add(NoteDenomination.ONE_HUNDRED, 1);
        Atm atm = readyAtm(1_000, inventory);
        authenticate(atm);

        atm.selectOperation(AtmOperation.WITHDRAW);
        assertThrows(InsufficientAtmCashException.class, () -> atm.withdraw(cents(200)));
        assertEquals(1, inventory.count(NoteDenomination.ONE_HUNDRED));
    }

    @Test
    void cannotMakeExactNotesIsRejected() {
        CashInventory inventory = new CashInventory().add(NoteDenomination.FIVE_HUNDRED, 1);
        Atm atm = readyAtm(1_000, inventory);
        authenticate(atm);

        atm.selectOperation(AtmOperation.WITHDRAW);
        assertThrows(ExactCashUnavailableException.class, () -> atm.withdraw(cents(300)));
        assertEquals(1, inventory.count(NoteDenomination.FIVE_HUNDRED));
    }

    @Test
    void depositIncreasesBalance() {
        Atm atm = readyAtm(1_000, stockedInventory());
        authenticate(atm);

        atm.selectOperation(AtmOperation.DEPOSIT);
        DepositResult result = atm.deposit(cents(250));

        assertEquals(cents(250), result.amountCents());
        assertEquals(cents(1_250), result.balanceAfterCents());
    }

    @Test
    void stateTransitionsRejectWithdrawalBeforeAuthentication() {
        Atm atm = readyAtm(1_000, stockedInventory());

        assertEquals("IDLE", atm.getStateName());
        assertThrows(InvalidStateException.class, () -> atm.withdraw(cents(100)));

        atm.insertCard(CARD);
        assertEquals("CARD_INSERTED", atm.getStateName());
        assertThrows(InvalidStateException.class, () -> atm.withdraw(cents(100)));

        PinAuthenticationResult auth = atm.enterPin("1234");
        assertTrue(auth.authenticated());
        assertEquals("AUTHENTICATED", atm.getStateName());

        atm.selectOperation(AtmOperation.WITHDRAW);
        atm.withdraw(cents(100));
        atm.ejectCard();
        assertEquals("IDLE", atm.getStateName());
    }

    private void authenticate(Atm atm) {
        atm.insertCard(CARD);
        assertTrue(atm.enterPin("1234").authenticated());
    }

    private static CashInventory stockedInventory() {
        return new CashInventory()
                .add(NoteDenomination.TWO_THOUSAND, 2)
                .add(NoteDenomination.FIVE_HUNDRED, 6)
                .add(NoteDenomination.ONE_HUNDRED, 14);
    }

    private static int cents(int rupees) {
        return rupees * 100;
    }
}
