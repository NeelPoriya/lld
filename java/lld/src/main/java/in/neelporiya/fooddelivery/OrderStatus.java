package in.neelporiya.fooddelivery;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * // DESIGN PATTERN: State machine. The legal lifecycle of an order lives in one place — the
 * transition table — instead of being re-derived by every mutation site.
 *
 * <pre>
 * PLACED -> ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED
 *   \___________\___________\_______________\__> CANCELLED (until it's out for delivery)
 * </pre>
 */
public enum OrderStatus {
    PLACED,
    ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(PLACED, EnumSet.of(ACCEPTED, CANCELLED));
        TRANSITIONS.put(ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
        TRANSITIONS.put(PREPARING, EnumSet.of(READY_FOR_PICKUP, CANCELLED));
        TRANSITIONS.put(READY_FOR_PICKUP, EnumSet.of(OUT_FOR_DELIVERY, CANCELLED));
        TRANSITIONS.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus next) {
        return TRANSITIONS.get(this).contains(next);
    }

    public boolean isTerminal() {
        return TRANSITIONS.get(this).isEmpty();
    }

    public Set<OrderStatus> allowedNext() {
        return Collections.unmodifiableSet(TRANSITIONS.get(this));
    }
}
