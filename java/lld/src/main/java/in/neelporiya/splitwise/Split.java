package in.neelporiya.splitwise;

/** One participant's share of an expense, in integer cents. */
public record Split(String userId, long amountCents) {
}
