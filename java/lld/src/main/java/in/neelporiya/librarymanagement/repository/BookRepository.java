package in.neelporiya.librarymanagement.repository;

import in.neelporiya.librarymanagement.model.Book;
import in.neelporiya.librarymanagement.model.BookItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * // DESIGN PATTERN: Repository — hides storage details from the facade.
 *
 * <p>// CONCURRENCY: maps are concurrent and per-book copy lists are copy-on-write, so search and
 * availability can take safe snapshots while checkouts mutate individual {@link BookItem}s atomically.
 */
public class BookRepository {

    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final Map<String, BookItem> itemsByBarcode = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<BookItem>> itemsByBook = new ConcurrentHashMap<>();

    public void save(Book book) {
        books.put(book.getId(), book);
    }

    public Optional<Book> findById(String bookId) {
        return Optional.ofNullable(books.get(bookId));
    }

    public List<Book> findAll() {
        return List.copyOf(books.values());
    }

    public void addItem(BookItem item) {
        itemsByBarcode.put(item.getBarcode(), item);
        itemsByBook.computeIfAbsent(item.getBookId(), ignored -> new CopyOnWriteArrayList<>()).add(item);
    }

    public Optional<BookItem> findItem(String barcode) {
        return Optional.ofNullable(itemsByBarcode.get(barcode));
    }

    public List<BookItem> findItemsByBook(String bookId) {
        return List.copyOf(itemsByBook.getOrDefault(bookId, new CopyOnWriteArrayList<>()));
    }

    public List<BookItem> findAvailableItems(String bookId) {
        return findItemsByBook(bookId).stream()
                .filter(BookItem::isAvailable)
                .toList();
    }

    public long availableCount(String bookId) {
        return findAvailableItems(bookId).size();
    }
}
