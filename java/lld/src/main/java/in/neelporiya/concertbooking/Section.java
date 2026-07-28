package in.neelporiya.concertbooking;

import java.util.Objects;

/**
 * A named group of seats sharing a pricing tier.
 */
public class Section implements Identifiable {

    private final String id;
    private final String name;
    private final String tier;
    private final long basePriceCents;

    public Section(String id, String name, String tier, long basePriceCents) {
        if (basePriceCents < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.tier = Objects.requireNonNull(tier, "tier");
        this.basePriceCents = basePriceCents;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTier() {
        return tier;
    }

    public long getBasePriceCents() {
        return basePriceCents;
    }
}
