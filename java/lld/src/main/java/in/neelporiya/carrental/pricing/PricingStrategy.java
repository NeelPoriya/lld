package in.neelporiya.carrental.pricing;

import in.neelporiya.carrental.model.AddOn;
import in.neelporiya.carrental.model.DateRange;
import in.neelporiya.carrental.model.Vehicle;

import java.math.BigDecimal;
import java.util.Set;

public interface PricingStrategy {

    BigDecimal calculate(Vehicle vehicle, DateRange range, Set<AddOn> addOns);
}
