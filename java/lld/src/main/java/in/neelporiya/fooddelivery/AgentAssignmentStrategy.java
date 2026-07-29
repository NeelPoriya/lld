package in.neelporiya.fooddelivery;

import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — RANK the free couriers for an order's pickup location. The dispatcher
 * then tries to atomically claim them in this order, so the strategy expresses preference without
 * having to win the race itself.
 */
public interface AgentAssignmentStrategy {

    /** Order the candidate agents best-first for a pickup at {@code restaurantLocation}. */
    List<DeliveryAgent> rank(List<DeliveryAgent> candidates, Location restaurantLocation);
}
