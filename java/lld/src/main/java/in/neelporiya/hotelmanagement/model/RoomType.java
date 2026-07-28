package in.neelporiya.hotelmanagement.model;

public enum RoomType {
    STANDARD(10_000),
    DELUXE(18_000),
    SUITE(35_000);

    private final long nightlyRateCents;

    RoomType(long nightlyRateCents) {
        this.nightlyRateCents = nightlyRateCents;
    }

    public long getNightlyRateCents() {
        return nightlyRateCents;
    }
}
