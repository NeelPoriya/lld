package in.neelporiya.stackoverflow.reputation;

import in.neelporiya.stackoverflow.event.PostEventListener;
import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Post;
import in.neelporiya.stackoverflow.model.User;
import in.neelporiya.stackoverflow.model.VoteChange;

/**
 * Applies {@link ReputationRules} to users in reaction to vote/accept events (Observer).
 *
 * <p>// INTERVIEW INSIGHT: reputation is computed from the vote <em>transition</em>. For a vote we
 * apply {@code pointsFor(current) - pointsFor(previous)}. This single formula correctly handles a
 * fresh upvote (+10), a retraction (−10), and a flip UP→DOWN (−12) — no special cases.
 */
public class ReputationManager implements PostEventListener {

    private final ReputationRules rules;

    public ReputationManager(ReputationRules rules) {
        this.rules = rules;
    }

    @Override
    public void onVote(Post post, User author, VoteChange change) {
        int delta = rules.pointsFor(post, change.current()) - rules.pointsFor(post, change.previous());
        if (delta != 0) {
            author.addReputation(delta);
        }
    }

    @Override
    public void onAnswerAccepted(Answer accepted, Answer previouslyAccepted) {
        accepted.getAuthor().addReputation(rules.acceptedAnswerPoints());
        if (previouslyAccepted != null) {
            previouslyAccepted.getAuthor().addReputation(-rules.acceptedAnswerPoints());
        }
    }
}
