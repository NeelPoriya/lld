package in.neelporiya.vendingmachine;

import java.util.List;
import java.util.Objects;

/** Deterministic outcome of a successful purchase. */
public record DispenseResult(Product product, List<Denomination> change, int amountPaidCents) {
    public DispenseResult {
        Objects.requireNonNull(product, "product");
        change = List.copyOf(Objects.requireNonNull(change, "change"));
    }

    public int changeCents() {
        return change.stream().mapToInt(Denomination::cents).sum();
    }
}
