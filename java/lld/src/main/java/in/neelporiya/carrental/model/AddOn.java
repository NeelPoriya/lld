package in.neelporiya.carrental.model;

public enum AddOn {
    GPS(500),
    CHILD_SEAT(700),
    INSURANCE(1_200);

    private final long dailyRateCents;

    AddOn(long dailyRateCents) {
        this.dailyRateCents = dailyRateCents;
    }

    public long getDailyRateCents() {
        return dailyRateCents;
    }
}
