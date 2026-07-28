package in.neelporiya.librarymanagement.search;

import in.neelporiya.librarymanagement.model.Book;

import java.util.List;

/** // DESIGN PATTERN: Strategy — title/author/subject search are swappable catalog policies. */
public interface SearchStrategy {
    List<Book> search(List<Book> books, String query);
}
