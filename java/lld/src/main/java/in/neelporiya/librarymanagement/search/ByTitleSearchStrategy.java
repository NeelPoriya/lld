package in.neelporiya.librarymanagement.search;

import in.neelporiya.librarymanagement.model.Book;

import java.util.List;
import java.util.Locale;

public class ByTitleSearchStrategy implements SearchStrategy {
    @Override
    public List<Book> search(List<Book> books, String query) {
        String needle = query.toLowerCase(Locale.ROOT);
        return books.stream()
                .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }
}
