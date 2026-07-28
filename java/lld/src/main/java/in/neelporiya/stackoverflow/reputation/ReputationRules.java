package in.neelporiya.stackoverflow.reputation;

import in.neelporiya.stackoverflow.model.Post;
import in.neelporiya.stackoverflow.model.VoteType;

/**
 * // DESIGN PATTERN: Strategy — the reputation policy. Different sites (or A/B experiments) can plug
 * in different numbers without touching the manager or the model.
 */
public interface ReputationRules {

    /** Reputation the author earns for a single vote of {@code voteType} on {@code post}. {@code null} => 0. */
    int pointsFor(Post post, VoteType voteType);

    /** Reputation for having an answer accepted. */
    int acceptedAnswerPoints();
}
