package in.neelporiya.movieticket;

import java.util.EnumMap;
import java.util.Map;

public class SeatTypePricingStrategy implements SeatPricingStrategy {

    private final Map<SeatType, Long> overrides;

    public SeatTypePricingStrategy() {
        this.overrides = Map.of();
    }

    public SeatTypePricingStrategy(Map<SeatType, Long> overrides) {
        this.overrides = new EnumMap<>(overrides);
    }

    @Override
    public long priceCents(Show show, Seat seat) {
        return overrides.getOrDefault(seat.getType(), seat.getBasePriceCents());
    }
}
