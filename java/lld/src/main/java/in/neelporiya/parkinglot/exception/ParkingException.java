package in.neelporiya.parkinglot.exception;

/**
 * Base unchecked exception for the parking domain.
 *
 * <p>// INTERVIEW INSIGHT: a single package-level supertype lets callers catch the whole domain with
 * one {@code catch (ParkingException e)} while still allowing fine-grained handling of subtypes. We
 * use unchecked exceptions because these are programming/flow conditions the caller usually cannot
 * recover from mid-transaction.
 */
public class ParkingException extends RuntimeException {
    public ParkingException(String message) {
        super(message);
    }
}
