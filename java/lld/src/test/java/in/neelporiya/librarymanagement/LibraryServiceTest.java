package in.neelporiya.librarymanagement;

import in.neelporiya.librarymanagement.exception.MaxBooksExceededException;
import in.neelporiya.librarymanagement.exception.NoAvailableCopyException;
import in.neelporiya.librarymanagement.model.Book;
import in.neelporiya.librarymanagement.model.Hold;
import in.neelporiya.librarymanagement.model.Loan;
import in.neelporiya.librarymanagement.model.Member;
import in.neelporiya.librarymanagement.model.ReturnReceipt;
import in.neelporiya.librarymanagement.search.ByAuthorSearchStrategy;
import in.neelporiya.librarymanagement.search.BySubjectSearchStrategy;
import in.neelporiya.librarymanagement.search.ByTitleSearchStrategy;
import in.neelporiya.librarymanagement.service.LibraryService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

    private final MutableClock clock = MutableClock.atEpoch();
    private final AtomicInteger ids = new AtomicInteger();

    private LibraryService service() {
        return LibraryService.builder()
                .clock(clock)
                .idGenerator(() -> "ID-" + ids.incrementAndGet())
                .maxBooksPerMember(2)
                .build();
    }

    private Book book(String id, String title, String author, String subject) {
        return Book.builder()
                .id(id)
                .isbn("ISBN-" + id)
                .title(title)
                .author(author)
                .subject(subject)
                .build();
    }

    @Test
    void searchesCatalogByTitleAuthorAndSubject() {
        LibraryService library = service();
        library.addBook(book("b1", "Clean Code", "Robert Martin", "Programming"));
        library.addBook(book("b2", "Designing Data-Intensive Applications", "Martin Kleppmann", "Distributed Systems"));

        assertEquals(List.of("b1"), library.search("clean", new ByTitleSearchStrategy()).stream().map(Book::getId).toList());
        assertEquals(List.of("b2"), library.search("klepp", new ByAuthorSearchStrategy()).stream().map(Book::getId).toList());
        assertEquals(List.of("b1"), library.search("program", new BySubjectSearchStrategy()).stream().map(Book::getId).toList());
    }

    @Test
    void checkoutReducesAvailabilityAndReturnFreesCopy() {
        LibraryService library = service();
        library.addBook(book("b1", "Refactoring", "Martin Fowler", "Programming"));
        library.addBookCopy("b1", "BC-1");
        Member member = library.registerMember("Asha");

        Loan loan = library.checkout(member.getId(), "b1");
        assertEquals(0L, library.availableCopies("b1"));

        ReturnReceipt receipt = library.returnBook(loan.getId());
        assertEquals("BC-1", receipt.barcode());
        assertEquals(1L, library.availableCopies("b1"));
        assertEquals(0, member.getActiveLoanCount());
    }

    @Test
    void overdueFineUsesInjectedMutableClock() {
        // TESTABILITY: no Thread.sleep. Jump the injected clock past the due date and assert cents.
        LibraryService library = service();
        library.addBook(book("b1", "Domain-Driven Design", "Eric Evans", "Architecture"));
        library.addBookCopy("b1", "BC-1");
        Member member = library.registerMember("Dev");
        Loan loan = library.checkout(member.getId(), "b1");

        clock.advance(Duration.ofDays(16));
        ReturnReceipt receipt = library.returnBook(loan.getId());

        assertEquals(200L, receipt.fineCents()); // default 100 cents/day, 2 days late
        assertEquals(200L, loan.getFineCents());
    }

    @Test
    void maxBooksPerMemberIsEnforced() {
        LibraryService library = LibraryService.builder()
                .clock(clock)
                .idGenerator(() -> "ID-" + ids.incrementAndGet())
                .maxBooksPerMember(1)
                .build();
        library.addBook(book("b1", "Book One", "Author", "Subject"));
        library.addBook(book("b2", "Book Two", "Author", "Subject"));
        library.addBookCopy("b1", "BC-1");
        library.addBookCopy("b2", "BC-2");
        Member member = library.registerMember("Reader");

        library.checkout(member.getId(), "b1");

        assertThrows(MaxBooksExceededException.class, () -> library.checkout(member.getId(), "b2"));
    }

    @Test
    void holdIsPlacedWhenNoCopiesAndNextMemberIsNotifiedOnReturn() {
        // DESIGN PATTERN: Observer — test listener receives the hold notification without polling.
        List<String> notifications = new ArrayList<>();
        LibraryService library = service();
        library.addHoldNotificationListener((hold, item) ->
                notifications.add(hold.getMemberId() + ":" + item.getBarcode()));
        library.addBook(book("b1", "Patterns", "Gamma", "Design"));
        library.addBookCopy("b1", "BC-1");
        Member borrower = library.registerMember("Borrower");
        Member waiter = library.registerMember("Waiter");
        Loan loan = library.checkout(borrower.getId(), "b1");

        assertThrows(NoAvailableCopyException.class, () -> library.checkout(waiter.getId(), "b1"));
        Hold hold = library.placeHold(waiter.getId(), "b1");
        assertFalse(hold.isNotified());

        library.returnBook(loan.getId());

        assertTrue(hold.isNotified());
        assertEquals("BC-1", hold.getReservedBarcode());
        assertEquals(List.of(waiter.getId() + ":BC-1"), notifications);
        assertEquals(0L, library.availableCopies("b1"), "returned copy is reserved for the hold");
    }
}
