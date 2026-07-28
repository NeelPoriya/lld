package in.neelporiya.digitalwallet;

import java.time.Instant;

/** An immutable append-only ledger entry. {@code balanceAfter} makes the ledger self-auditing. */
public record WalletTransaction(
        String id,
        String walletId,
        TransactionType type,
        Money amount,
        Money balanceAfter,
        Instant at,
        String reference) {
}
