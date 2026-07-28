package in.neelporiya.hotelmanagement.pricing;

import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.StayRange;

import java.util.Objects;

public final class NightlyRatePricingStrategy implements PricingStrategy {

    @Override
    public long calculateCents(Room room, StayRange range) {
        Objects.requireNonNull(room, "room");
        Objects.requireNonNull(range, "range");
        // DESIGN PATTERN: Strategy keeps pricing swappable; money is integer cents, never double.
        return range.nights() * room.getType().getNightlyRateCents();
    }
}
