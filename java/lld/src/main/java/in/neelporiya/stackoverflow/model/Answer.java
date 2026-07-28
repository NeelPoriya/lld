package in.neelporiya.stackoverflow.model;

import java.time.Instant;

public class Answer extends Post {

    private final Question question;
    private volatile boolean accepted;

    public Answer(String id, User author, Question question, String body, Instant createdAt) {
        super(id, author, body, createdAt);
        this.question = question;
    }

    // Package-private: only Question.accept flips this, keeping the accepted-answer invariant in one place.
    void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Question getQuestion() {
        return question;
    }
}
