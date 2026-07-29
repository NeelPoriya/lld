package in.neelporiya.onlineshopping;

/** Payment gateway input. */
public record PaymentRequest(String orderId, long amountCents) {
}
