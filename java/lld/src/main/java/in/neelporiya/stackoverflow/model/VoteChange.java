package in.neelporiya.stackoverflow.model;

/**
 * The result of casting a vote: what the user's vote was before and what it is now (either may be
 * {@code null} meaning "no vote").
 *
 * <p>// INTERVIEW INSIGHT: returning the <em>transition</em> (not just the new value) is what lets
 * the reputation subsystem compute an exact delta — e.g. flipping UP→DOWN on an answer is
 * {@code (-2) - (+10) = -12}. This is why toggling votes doesn't corrupt reputation.
 */
public record VoteChange(VoteType previous, VoteType current) {

    public boolean isNoOp() {
        return previous == current;
    }
}
