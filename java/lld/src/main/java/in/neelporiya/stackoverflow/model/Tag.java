package in.neelporiya.stackoverflow.model;

/**
 * A normalized tag. Using a {@code record} with a compact constructor guarantees "Java" and "java"
 * are the same tag, and gives us value equality for free (so {@code Set<Tag>} de-dupes correctly).
 */
public record Tag(String name) {
    public Tag {
        name = name.trim().toLowerCase();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("tag name must not be blank");
        }
    }
}
