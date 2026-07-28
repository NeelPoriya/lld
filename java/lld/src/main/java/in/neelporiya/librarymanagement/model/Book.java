package in.neelporiya.librarymanagement.model;

import java.util.Objects;

/**
 * The logical catalog entry. Physical inventory lives in {@link BookItem} copies.
 *
 * <p>// DESIGN PATTERN: Builder — interview objects often grow optional metadata; the builder keeps
 * creation readable without telescoping constructors.
 */
public class Book {

    private final String id;
    private final String isbn;
    private final String title;
    private final String author;
    private final String subject;

    private Book(Builder builder) {
        this.id = builder.id;
        this.isbn = builder.isbn;
        this.title = builder.title;
        this.author = builder.author;
        this.subject = builder.subject;
    }

    public String getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String isbn;
        private String title;
        private String author;
        private String subject;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Book build() {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(isbn, "isbn");
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(author, "author");
            Objects.requireNonNull(subject, "subject");
            return new Book(this);
        }
    }
}
