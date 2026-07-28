package in.neelporiya.concertbooking;

public class SeatUnavailableException extends BookingException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
