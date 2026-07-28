package in.neelporiya.hotelmanagement.pricing;

import in.neelporiya.hotelmanagement.model.Room;
import in.neelporiya.hotelmanagement.model.StayRange;

public interface PricingStrategy {

    long calculateCents(Room room, StayRange range);
}
