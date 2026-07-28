package in.neelporiya.splitwise;

/** A settlement instruction produced by debt simplification: {@code from} should pay {@code to}. */
public record Settlement(String from, String to, long amountCents) {
}
