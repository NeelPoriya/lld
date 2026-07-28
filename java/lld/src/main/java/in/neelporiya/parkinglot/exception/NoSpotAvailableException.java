package in.neelporiya.parkinglot.exception;

/** Thrown when no spot can accommodate the incoming vehicle (lot full for that size). */
public class NoSpotAvailableException extends ParkingException {
    public NoSpotAvailableException(String message) {
        super(message);
    }
}
