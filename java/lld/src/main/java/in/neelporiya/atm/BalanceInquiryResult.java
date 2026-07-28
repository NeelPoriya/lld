package in.neelporiya.atm;

import java.time.Instant;

public record BalanceInquiryResult(String accountId, int balanceCents, Instant timestamp) {
}
