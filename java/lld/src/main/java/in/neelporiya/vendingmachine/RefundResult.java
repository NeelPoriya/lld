package in.neelporiya.vendingmachine;

import java.util.List;
import java.util.Objects;

/** Deterministic outcome of a cancel/refund operation. */
public record RefundResult(List<Denomination> returnedMoney) {
    public RefundResult {
        returnedMoney = List.copyOf(Objects.requireNonNull(returnedMoney, "returnedMoney"));
    }

    public int totalCents() {
        return returnedMoney.stream().mapToInt(Denomination::cents).sum();
    }
}
