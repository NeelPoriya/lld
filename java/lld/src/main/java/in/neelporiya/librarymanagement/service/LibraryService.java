package in.neelporiya.librarymanagement.service;

import in.neelporiya.librarymanagement.exception.BookNotFoundException;
import in.neelporiya.librarymanagement.exception.LoanNotFoundException;
import in.neelporiya.librarymanagement.exception.MaxBooksExceededException;
import in.neelporiya.librarymanagement.exception.MemberNotFoundException;
import in.neelporiya.librarymanagement.exception.NoAvailableCopyException;
import in.neelporiya.librarymanagement.fine.FineStrategy;
import in.neelporiya.librarymanagement.fine.PerDayLateFineStrategy;
import in.neelporiya.librarymanagement.model.Book;
import in.neelporiya.librarymanagement.model.BookItem;
import in.neelporiya.librarymanagement.model.Hold;
import in.neelporiya.librarymanagement.model.Loan;
import in.neelporiya.librarymanagement.model.Member;
import in.neelporiya.librarymanagement.model.ReturnReceipt;
import in.neelporiya.librarymanagement.observer.HoldNotificationListener;
import in.neelporiya.librarymanagement.repository.BookRepository;
import in.neelporiya.librarymanagement.repository.HoldRepository;
import in.neelporiya.librarymanagement.repository.LoanRepository;
import in.neelporiya.librarymanagement.repository.MemberRepository;
import in.neelporiya.librarymanagement.search.SearchStrategy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — one interview-friendly API over catalog, members, loans, holds,
 * strategies and repositories.
 *
 * <p>// TESTABILITY: {@link Clock} and the id {@link Supplier} are injected. Tests can advance a
 * MutableClock to make a loan overdue instantly and assert exact integer-cent fines.
 *
 * <p>// EXTENSIBILITY: new search or fine policies are added by implementing strategy interfaces,
 * not by editing checkout/return code.
 */
public class LibraryService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final HoldRepository holdRepository;
    private final FineStrategy fineStrategy;
    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final int maxBooksPerMember;
    private final Duration loanPeriod;
    private final List<HoldNotificationListener> listeners;

    private LibraryService(Builder builder) {
        this.bookRepository = builder.bookRepository;
        this.memberRepository = builder.memberRepository;
        this.loanRepository = builder.loanRepository;
        this.holdRepository = builder.holdRepository;
        this.fineStrategy = builder.fineStrategy;
        this.clock = builder.clock;
        this.idGenerator = builder.idGenerator;
        this.maxBooksPerMember = builder.maxBooksPerMember;
        this.loanPeriod = builder.loanPeriod;
        this.listeners = new CopyOnWriteArrayList<>(builder.listeners);
    }

    public static LibraryService createDefault() {
        return builder().build();
    }

    public Book addBook(Book book) {
        bookRepository.save(Objects.requireNonNull(book, "book"));
        return book;
    }

    public Book addBook(String isbn, String title, String author, String subject) {
        Book book = Book.builder()
                .id(idGenerator.get())
                .isbn(isbn)
                .title(title)
                .author(author)
                .subject(subject)
                .build();
        return addBook(book);
    }

    public BookItem addBookCopy(String bookId, String barcode) {
        requireBook(bookId);
        BookItem item = new BookItem(barcode, bookId);
        bookRepository.addItem(item);
        return item;
    }

    public boolean removeBookCopy(String barcode) {
        BookItem item = bookRepository.findItem(barcode)
                .orElseThrow(() -> new BookNotFoundException("No copy with barcode " + barcode));
        return item.markRemovedIfAvailable();
    }

    public Member registerMember(String name) {
        Member member = new Member(idGenerator.get(), name);
        memberRepository.save(member);
        return member;
    }

    public List<Book> search(String query, SearchStrategy strategy) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(strategy, "strategy");
        return strategy.search(bookRepository.findAll(), query);
    }

    /**
     * Check out a copy of a book for a member.
     *
     * <p>// CONCURRENCY: max-book capacity is claimed first with an atomic member counter. Then every
     * visible available copy is attempted with {@link BookItem#tryCheckout()}; if another thread won
     * the same last copy, CAS returns false and we continue. No copy can be loaned twice.
     */
    public Loan checkout(String memberId, String bookId) {
        Member member = requireMember(memberId);
        requireBook(bookId);
        if (!member.tryAcquireBorrowSlot(maxBooksPerMember)) {
            throw new MaxBooksExceededException("Member " + memberId + " reached max books " + maxBooksPerMember);
        }

        try {
            Loan reservedLoan = checkoutReservedIfPresent(member, bookId);
            if (reservedLoan != null) {
                return reservedLoan;
            }

            for (BookItem item : bookRepository.findAvailableItems(bookId)) {
                if (item.tryCheckout()) {
                    return createLoan(member.getId(), item);
                }
            }
            throw new NoAvailableCopyException("No available copy for book " + bookId + "; member may place a hold");
        } catch (RuntimeException e) {
            member.releaseBorrowSlot();
            throw e;
        }
    }

    /**
     * Check out one explicitly selected barcode.
     *
     * <p>// CONCURRENCY: this is the narrowest form of the last-copy race. Every caller targets the
     * same {@link BookItem}; {@code item.tryCheckout()} is still a CAS, so exactly one caller can move
     * that specific copy from AVAILABLE to CHECKED_OUT.
     */
    public Loan checkoutCopy(String memberId, String barcode) {
        Member member = requireMember(memberId);
        BookItem item = bookRepository.findItem(barcode)
                .orElseThrow(() -> new BookNotFoundException("No copy with barcode " + barcode));
        requireBook(item.getBookId());
        if (!member.tryAcquireBorrowSlot(maxBooksPerMember)) {
            throw new MaxBooksExceededException("Member " + memberId + " reached max books " + maxBooksPerMember);
        }

        try {
            if (!item.tryCheckout()) {
                throw new NoAvailableCopyException("Copy " + barcode + " is not available");
            }
            return createLoan(member.getId(), item);
        } catch (RuntimeException e) {
            member.releaseBorrowSlot();
            throw e;
        }
    }

    public Hold placeHold(String memberId, String bookId) {
        requireMember(memberId);
        requireBook(bookId);
        if (availableCopies(bookId) > 0) {
            throw new IllegalStateException("A hold is only needed when no copies are available");
        }
        Hold hold = new Hold(idGenerator.get(), bookId, memberId, clock.instant());
        holdRepository.save(hold);
        return hold;
    }

    /**
     * Return a loan, compute the overdue fine in integer cents, and notify the next hold if present.
     *
     * <p>// CONCURRENCY: {@code loanRepository.removeActive(loanId)} is the linearization point for
     * returns. If two librarians scan the same loan, exactly one gets the active loan; the other is
     * rejected. The copy then moves from CHECKED_OUT to AVAILABLE or RESERVED with a CAS.
     */
    public ReturnReceipt returnBook(String loanId) {
        Loan loan = loanRepository.removeActive(loanId);
        if (loan == null) {
            throw new LoanNotFoundException("Unknown or already returned loan " + loanId);
        }

        Instant returnedAt = clock.instant();
        long fineCents = fineStrategy.calculateFineCents(loan, returnedAt);
        loan.close(returnedAt, fineCents);
        requireMember(loan.getMemberId()).releaseBorrowSlot();

        BookItem item = bookRepository.findItem(loan.getBarcode())
                .orElseThrow(() -> new BookNotFoundException("No copy with barcode " + loan.getBarcode()));
        Hold nextHold = holdRepository.pollNextWaiting(loan.getBookId());
        if (nextHold == null) {
            item.markReturnedAvailable();
        } else if (item.markReturnedReservedForHold(nextHold.getId())) {
            nextHold.markNotified(returnedAt, item.getBarcode());
            listeners.forEach(listener -> listener.onHoldAvailable(nextHold, item));
        } else {
            item.markReturnedAvailable();
        }

        return new ReturnReceipt(loan.getId(), loan.getBarcode(), returnedAt, fineCents);
    }

    public long availableCopies(String bookId) {
        requireBook(bookId);
        return bookRepository.availableCount(bookId);
    }

    public List<Loan> activeLoansForMember(String memberId) {
        requireMember(memberId);
        return loanRepository.findActiveByMember(memberId);
    }

    public void addHoldNotificationListener(HoldNotificationListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private Loan checkoutReservedIfPresent(Member member, String bookId) {
        return holdRepository.findNotifiedHold(member.getId(), bookId)
                .flatMap(hold -> bookRepository.findItem(hold.getReservedBarcode())
                        .filter(item -> item.tryCheckoutReserved(hold.getId()))
                        .map(item -> {
                            holdRepository.remove(hold.getId());
                            return createLoan(member.getId(), item);
                        }))
                .orElse(null);
    }

    private Loan createLoan(String memberId, BookItem item) {
        Instant checkedOutAt = clock.instant();
        Loan loan = new Loan(idGenerator.get(), memberId, item.getBookId(), item.getBarcode(), checkedOutAt, checkedOutAt.plus(loanPeriod));
        loanRepository.saveActive(loan);
        return loan;
    }

    private Book requireBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book with id " + bookId));
    }

    private Member requireMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("No member with id " + memberId));
    }

    public static Builder builder() {
        return new Builder();
    }

    /** // DESIGN PATTERN: Builder — wires repositories, strategies, limits, clock and listeners. */
    public static final class Builder {
        private BookRepository bookRepository = new BookRepository();
        private MemberRepository memberRepository = new MemberRepository();
        private LoanRepository loanRepository = new LoanRepository();
        private HoldRepository holdRepository = new HoldRepository();
        private FineStrategy fineStrategy = new PerDayLateFineStrategy(100);
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();
        private int maxBooksPerMember = 5;
        private Duration loanPeriod = Duration.ofDays(14);
        private final List<HoldNotificationListener> listeners = new ArrayList<>();

        public Builder bookRepository(BookRepository bookRepository) {
            this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository");
            return this;
        }

        public Builder memberRepository(MemberRepository memberRepository) {
            this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository");
            return this;
        }

        public Builder loanRepository(LoanRepository loanRepository) {
            this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository");
            return this;
        }

        public Builder holdRepository(HoldRepository holdRepository) {
            this.holdRepository = Objects.requireNonNull(holdRepository, "holdRepository");
            return this;
        }

        public Builder fineStrategy(FineStrategy fineStrategy) {
            this.fineStrategy = Objects.requireNonNull(fineStrategy, "fineStrategy");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
            return this;
        }

        public Builder maxBooksPerMember(int maxBooksPerMember) {
            if (maxBooksPerMember <= 0) {
                throw new IllegalArgumentException("maxBooksPerMember must be positive");
            }
            this.maxBooksPerMember = maxBooksPerMember;
            return this;
        }

        public Builder loanPeriod(Duration loanPeriod) {
            this.loanPeriod = Objects.requireNonNull(loanPeriod, "loanPeriod");
            return this;
        }

        public Builder addHoldNotificationListener(HoldNotificationListener listener) {
            this.listeners.add(Objects.requireNonNull(listener, "listener"));
            return this;
        }

        public LibraryService build() {
            return new LibraryService(this);
        }
    }
}
