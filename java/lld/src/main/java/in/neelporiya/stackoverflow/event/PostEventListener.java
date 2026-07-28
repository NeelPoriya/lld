package in.neelporiya.stackoverflow.event;

import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Post;
import in.neelporiya.stackoverflow.model.User;
import in.neelporiya.stackoverflow.model.VoteChange;

/**
 * // DESIGN PATTERN: Observer.
 *
 * <p>The service emits domain events; listeners react. This is how reputation stays decoupled from
 * posting: the {@code model} package never imports the {@code reputation} package (no cyclic
 * dependency), yet reputation still updates on every vote/accept.
 */
public interface PostEventListener {

    default void onVote(Post post, User author, VoteChange change) {
    }

    default void onAnswerAccepted(Answer accepted, Answer previouslyAccepted) {
    }
}
