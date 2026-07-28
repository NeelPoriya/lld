package in.neelporiya.stackoverflow.reputation;

import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Post;
import in.neelporiya.stackoverflow.model.VoteType;

/** The classic Stack Overflow-like numbers. */
public class DefaultReputationRules implements ReputationRules {

    @Override
    public int pointsFor(Post post, VoteType voteType) {
        if (voteType == null) {
            return 0;
        }
        boolean isAnswer = post instanceof Answer;
        return switch (voteType) {
            case UP -> isAnswer ? 10 : 5;
            case DOWN -> -2;
        };
    }

    @Override
    public int acceptedAnswerPoints() {
        return 15;
    }
}
