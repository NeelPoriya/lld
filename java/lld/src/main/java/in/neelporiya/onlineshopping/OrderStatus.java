package in.neelporiya.onlineshopping;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Order lifecycle with legal transitions encoded as data.
 *
 * <p>// DESIGN PATTERN: State machine — one map owns the workflow rules, instead of scattering
 * {@code if/else} checks across the service.
 */
public enum OrderStatus {
    PLACED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(PLACED, EnumSet.of(PAID, CANCELLED));
        ALLOWED.put(PAID, EnumSet.of(SHIPPED, CANCELLED));
        ALLOWED.put(SHIPPED, EnumSet.of(DELIVERED));
        ALLOWED.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
