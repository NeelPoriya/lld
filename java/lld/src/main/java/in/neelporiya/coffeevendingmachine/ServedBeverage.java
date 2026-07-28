package in.neelporiya.coffeevendingmachine;

import java.time.Instant;

/** Audit record of a served drink, timestamped from the injected clock. */
public record ServedBeverage(String name, Instant servedAt) {
}
