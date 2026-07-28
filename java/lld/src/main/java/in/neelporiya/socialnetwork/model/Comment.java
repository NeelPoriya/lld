package in.neelporiya.socialnetwork.model;

import java.time.Instant;
import java.util.Objects;

public record Comment(String id, User author, String text, Instant createdAt) {

    public Comment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}
