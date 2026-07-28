package in.neelporiya.parkinglot.exception;

/** Thrown when a ticket id is unknown or has already been used to exit. */
public class InvalidTicketException extends ParkingException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
