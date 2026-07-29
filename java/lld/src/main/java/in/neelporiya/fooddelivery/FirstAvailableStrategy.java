package in.neelporiya.fooddelivery;

import java.util.List;

/** Take couriers in registration order — a simple round-robin-ish default. */
public class FirstAvailableStrategy implements AgentAssignmentStrategy {

    @Override
    public List<DeliveryAgent> rank(List<DeliveryAgent> candidates, Location restaurantLocation) {
        return List.copyOf(candidates);
    }
}
