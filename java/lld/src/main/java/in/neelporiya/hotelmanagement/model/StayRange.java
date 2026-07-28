package in.neelporiya.hotelmanagement.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class StayRange {

    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public StayRange(LocalDate checkIn, LocalDate checkOut) {
        this.checkIn = Objects.requireNonNull(checkIn, "checkIn");
        this.checkOut = Objects.requireNonNull(checkOut, "checkOut");
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Stay range must be half-open with check-in before check-out");
        }
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean overlaps(StayRange other) {
        Objects.requireNonNull(other, "other");
        // CONCURRENCY: The atomic booking path depends on this exact half-open rule: in1 < out2 && in2 < out1.
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }
}
