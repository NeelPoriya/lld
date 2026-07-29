package in.neelporiya.fooddelivery;

import java.util.Comparator;
import java.util.List;

/** Prefer the closest courier to the restaurant. */
public class NearestAgentStrategy implements AgentAssignmentStrategy {

    @Override
    public List<DeliveryAgent> rank(List<DeliveryAgent> candidates, Location restaurantLocation) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(a -> a.getLocation().distanceTo(restaurantLocation)))
                .toList();
    }
}
