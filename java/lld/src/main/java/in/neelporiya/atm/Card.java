package in.neelporiya.atm;

import java.util.Objects;

/** A small value object that maps a physical card to exactly one account. */
public final class Card {

    private final String cardNumber;
    private final String accountId;

    public Card(String cardNumber, String accountId) {
        this.cardNumber = Objects.requireNonNull(cardNumber, "cardNumber");
        this.accountId = Objects.requireNonNull(accountId, "accountId");
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getAccountId() {
        return accountId;
    }
}
