package in.neelporiya.atm;

import java.time.Instant;

/** Records the selected operation and the deterministic time at which it was chosen. */
public record OperationSelection(AtmOperation operation, Instant selectedAt) {
}
