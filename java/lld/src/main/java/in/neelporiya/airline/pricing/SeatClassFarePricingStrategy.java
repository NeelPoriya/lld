package in.neelporiya.airline.pricing;

import in.neelporiya.airline.model.FlightInstance;
import in.neelporiya.airline.model.Seat;
import in.neelporiya.airline.model.SeatClass;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class SeatClassFarePricingStrategy implements FarePricingStrategy {

    private final Map<SeatClass, Integer> multipliersByClass;

    public SeatClassFarePricingStrategy() {
        this(Map.of(SeatClass.ECONOMY, 1, SeatClass.BUSINESS, 2, SeatClass.FIRST, 4));
    }

    public SeatClassFarePricingStrategy(Map<SeatClass, Integer> multipliersByClass) {
        this.multipliersByClass = Map.copyOf(Objects.requireNonNull(multipliersByClass, "multipliersByClass"));
    }

    @Override
    public BigDecimal price(FlightInstance flightInstance, Seat seat) {
        // DESIGN PATTERN: Strategy makes fare rules swappable for demand, coupons, or route-based pricing.
        Objects.requireNonNull(flightInstance, "flightInstance");
        Objects.requireNonNull(seat, "seat");
        long totalCents = Math.multiplyExact(seat.getBaseFareCents(), multipliersByClass.getOrDefault(seat.getSeatClass(), 1));
        return BigDecimal.valueOf(totalCents, 2);
    }
}
