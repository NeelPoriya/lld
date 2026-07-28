package in.neelporiya.atm;

import java.time.Instant;

public record DepositResult(String accountId, int amountCents, int balanceAfterCents, Instant timestamp) {
}
