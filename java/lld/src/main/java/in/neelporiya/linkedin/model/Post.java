package in.neelporiya.linkedin.model;

import java.time.Instant;
import java.util.Objects;

public class Post {

    private final String id;
    private final Member author;
    private final String text;
    private final Instant createdAt;

    public Post(String id, Member author, String text, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.author = Objects.requireNonNull(author, "author");
        this.text = Objects.requireNonNull(text, "text");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public String getId() {
        return id;
    }

    public Member getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
