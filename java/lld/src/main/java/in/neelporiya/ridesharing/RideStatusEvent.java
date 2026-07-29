package in.neelporiya.ridesharing;

import java.time.Instant;

public record RideStatusEvent(String rideId, String riderId, String driverId, RideStatus status, Instant at) {
}
