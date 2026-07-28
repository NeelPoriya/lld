package in.neelporiya.librarymanagement.fine;

import in.neelporiya.librarymanagement.model.Loan;

import java.time.Duration;
import java.time.Instant;

/** Computes integer-cent fines by rounding any partial overdue day up to a full day. */
public class PerDayLateFineStrategy implements FineStrategy {

    private final long centsPerLateDay;

    public PerDayLateFineStrategy(long centsPerLateDay) {
        if (centsPerLateDay < 0) {
            throw new IllegalArgumentException("fine cannot be negative");
        }
        this.centsPerLateDay = centsPerLateDay;
    }

    @Override
    public long calculateFineCents(Loan loan, Instant returnedAt) {
        if (!returnedAt.isAfter(loan.getDueAt())) {
            return 0;
        }
        long overdueMillis = Duration.between(loan.getDueAt(), returnedAt).toMillis();
        long daysLate = Math.max(1, (overdueMillis + Duration.ofDays(1).toMillis() - 1) / Duration.ofDays(1).toMillis());
        return daysLate * centsPerLateDay;
    }
}
