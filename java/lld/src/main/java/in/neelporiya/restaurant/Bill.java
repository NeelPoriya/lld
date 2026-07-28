package in.neelporiya.restaurant;

/** Immutable bill result. All money fields are integer cents. */
public record Bill(String orderId, long subtotalCents, long taxCents, long discountCents, long tipCents) {
    public Bill {
        if (subtotalCents < 0 || taxCents < 0 || discountCents < 0 || tipCents < 0) {
            throw new IllegalArgumentException("money values cannot be negative");
        }
    }

    public long totalCents() {
        return Math.max(0, subtotalCents + taxCents - discountCents + tipCents);
    }
}
