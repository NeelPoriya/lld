package in.neelporiya.carrental.pricing;

import in.neelporiya.carrental.model.AddOn;
import in.neelporiya.carrental.model.DateRange;
import in.neelporiya.carrental.model.Vehicle;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public final class PerDayPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculate(Vehicle vehicle, DateRange range, Set<AddOn> addOns) {
        // DESIGN PATTERN: Strategy keeps price calculation swappable without touching the facade.
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(range, "range");
        Set<AddOn> safeAddOns = addOns == null ? Set.of() : Set.copyOf(addOns);

        long dailyCents = vehicle.getType().getDailyRateCents()
                + safeAddOns.stream().mapToLong(AddOn::getDailyRateCents).sum();
        long totalCents = Math.multiplyExact(dailyCents, range.days());
        return BigDecimal.valueOf(totalCents, 2);
    }
}
