package in.neelporiya.carrental.model;

public enum VehicleType {
    ECONOMY(4, 2, 2_500),
    SUV(7, 4, 5_000),
    LUXURY(4, 3, 9_000),
    VAN(12, 6, 7_500);

    private final int seats;
    private final int bags;
    private final long dailyRateCents;

    VehicleType(int seats, int bags, long dailyRateCents) {
        this.seats = seats;
        this.bags = bags;
        this.dailyRateCents = dailyRateCents;
    }

    public int getSeats() {
        return seats;
    }

    public int getBags() {
        return bags;
    }

    public long getDailyRateCents() {
        return dailyRateCents;
    }
}
