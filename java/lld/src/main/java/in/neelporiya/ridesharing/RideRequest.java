package in.neelporiya.ridesharing;

public record RideRequest(String rideId, Rider rider, Location pickup, Location drop) {
}
