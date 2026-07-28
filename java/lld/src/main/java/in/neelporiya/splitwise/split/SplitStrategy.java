package in.neelporiya.splitwise.split;

import in.neelporiya.splitwise.Split;

import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — how an expense's total is divided among participants. Equal, exact
 * and percentage splits are interchangeable; adding a new scheme never touches the service.
 *
 * <p>Implementations MUST return shares that sum exactly to {@code totalCents} (rounding remainders
 * distributed deterministically), so no cents are created or lost.
 */
public interface SplitStrategy {

    List<Split> split(long totalCents, List<String> participants);
}
