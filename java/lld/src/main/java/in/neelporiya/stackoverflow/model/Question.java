package in.neelporiya.stackoverflow.model;

import in.neelporiya.stackoverflow.exception.UnauthorizedActionException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Question extends Post {

    private final String title;
    private final Set<Tag> tags;
    private final List<Answer> answers = new CopyOnWriteArrayList<>();
    private volatile Answer acceptedAnswer;

    public Question(String id, User author, String title, String body, Set<Tag> tags, Instant createdAt) {
        super(id, author, body, createdAt);
        this.title = title;
        this.tags = Set.copyOf(tags);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    /**
     * Accept an answer.
     *
     * <p>// CONCURRENCY: guarded by the question's lock so two simultaneous "accept" clicks can't
     * both flip the accepted flag. Only the question's author may accept (authorization check).
     */
    public synchronized AcceptResult accept(Answer answer, User byUser) {
        if (!getAuthor().equals(byUser)) {
            throw new UnauthorizedActionException("Only the question author can accept an answer");
        }
        if (!answers.contains(answer)) {
            throw new IllegalArgumentException("That answer does not belong to this question");
        }
        if (acceptedAnswer == answer) {
            return AcceptResult.unchanged();
        }
        Answer previous = acceptedAnswer;
        if (previous != null) {
            previous.setAccepted(false);
        }
        answer.setAccepted(true);
        acceptedAnswer = answer;
        return AcceptResult.changed(previous);
    }

    public String getTitle() {
        return title;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public List<Answer> getAnswers() {
        return List.copyOf(answers);
    }

    public Answer getAcceptedAnswer() {
        return acceptedAnswer;
    }
}
