package in.neelporiya.restaurant;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * // DESIGN PATTERN: State machine — legal order transitions are encoded as data, not scattered ifs.
 *
 * <p>// INTERVIEW INSIGHT: {@code PAID} is terminal because its allowed-transition set is empty; the
 * system cannot accidentally reopen a completed order.
 */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(PLACED, EnumSet.of(PREPARING));
        ALLOWED.put(PREPARING, EnumSet.of(READY));
        ALLOWED.put(READY, EnumSet.of(SERVED));
        ALLOWED.put(SERVED, EnumSet.of(PAID));
        ALLOWED.put(PAID, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
