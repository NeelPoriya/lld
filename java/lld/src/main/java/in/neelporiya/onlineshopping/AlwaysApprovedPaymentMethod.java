package in.neelporiya.onlineshopping;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

/** Test-friendly payment method that approves every payment. */
public class AlwaysApprovedPaymentMethod implements PaymentMethod {

    private final Clock clock;
    private final Supplier<String> paymentIdGenerator;

    public AlwaysApprovedPaymentMethod(Clock clock, Supplier<String> paymentIdGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.paymentIdGenerator = Objects.requireNonNull(paymentIdGenerator, "paymentIdGenerator");
    }

    @Override
    public PaymentReceipt pay(PaymentRequest request) {
        return new PaymentReceipt(paymentIdGenerator.get(), request.orderId(), request.amountCents(), true,
                clock.instant());
    }
}
