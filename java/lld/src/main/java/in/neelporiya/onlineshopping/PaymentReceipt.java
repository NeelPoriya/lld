package in.neelporiya.onlineshopping;

import java.time.Instant;

/** Result object returned by a payment strategy. */
public record PaymentReceipt(String paymentId, String orderId, long amountCents, boolean approved, Instant paidAt) {
}
