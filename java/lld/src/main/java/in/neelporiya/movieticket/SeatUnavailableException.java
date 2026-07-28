package in.neelporiya.movieticket;

public class SeatUnavailableException extends BookingException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
