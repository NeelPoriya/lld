package in.neelporiya.stackoverflow.model;

import java.time.Instant;

public record Comment(String id, User author, String text, Instant createdAt) {
}
