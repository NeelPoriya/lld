package in.neelporiya.stackoverflow.model;

/**
 * Outcome of an "accept answer" call. {@code changed} is false when the answer was already the
 * accepted one (idempotent), so the reputation subsystem knows to do nothing.
 */
public record AcceptResult(boolean changed, Answer previouslyAccepted) {

    public static AcceptResult unchanged() {
        return new AcceptResult(false, null);
    }

    public static AcceptResult changed(Answer previouslyAccepted) {
        return new AcceptResult(true, previouslyAccepted);
    }
}
