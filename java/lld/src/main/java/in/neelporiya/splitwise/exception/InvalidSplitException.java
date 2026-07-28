package in.neelporiya.splitwise.exception;

/** Thrown when a split is malformed (shares don't sum to the total, percentages != 100, etc.). */
public class InvalidSplitException extends RuntimeException {
    public InvalidSplitException(String message) {
        super(message);
    }
}
