package in.neelporiya.librarymanagement.fine;

import in.neelporiya.librarymanagement.model.Loan;

import java.time.Instant;

/** // DESIGN PATTERN: Strategy — per-day, grace-period, or member-tier fines can be swapped. */
public interface FineStrategy {
    long calculateFineCents(Loan loan, Instant returnedAt);
}
