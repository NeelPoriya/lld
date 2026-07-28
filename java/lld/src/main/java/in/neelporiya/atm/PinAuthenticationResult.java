package in.neelporiya.atm;

/** Return object makes PIN outcomes assertable without parsing exception text. */
public record PinAuthenticationResult(boolean authenticated, boolean locked, int attemptsRemaining) {
}
