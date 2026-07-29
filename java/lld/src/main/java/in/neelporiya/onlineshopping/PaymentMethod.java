package in.neelporiya.onlineshopping;

/** // DESIGN PATTERN: Strategy — card, UPI, wallet, COD, etc. can share this checkout seam. */
public interface PaymentMethod {
    PaymentReceipt pay(PaymentRequest request);
}
