package in.neelporiya.fooddelivery;

import java.math.BigDecimal;

/** The priced breakdown of an order. */
public record Bill(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal tax, BigDecimal total) {
}
