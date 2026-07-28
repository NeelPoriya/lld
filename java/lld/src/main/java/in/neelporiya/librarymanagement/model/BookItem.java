package in.neelporiya.librarymanagement.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One physical copy identified by a unique barcode.
 *
 * <p>// CONCURRENCY: checkout is a per-copy Compare-And-Swap. The common broken implementation is
 * "if available then set checked out"; two threads can pass the if-check. Here the check and state
 * transition are one atomic CAS, so exactly one member can claim the last copy.
 *
 * <p>// INTERVIEW INSIGHT: availability screens may be stale snapshots, but the write path is still
 * correct because {@code tryCheckout()} re-validates ownership atomically at the copy itself.
 */
public class BookItem {

    private final String barcode;
    private final String bookId;
    private final AtomicReference<State> state = new AtomicReference<>(new State(BookItemStatus.AVAILABLE, null));

    public BookItem(String barcode, String bookId) {
        this.barcode = Objects.requireNonNull(barcode, "barcode");
        this.bookId = Objects.requireNonNull(bookId, "bookId");
    }

    public boolean tryCheckout() {
        while (true) {
            State current = state.get();
            if (current.status() != BookItemStatus.AVAILABLE) {
                return false;
            }
            if (state.compareAndSet(current, new State(BookItemStatus.CHECKED_OUT, null))) {
                return true;
            }
        }
    }

    public boolean tryCheckoutReserved(String holdId) {
        Objects.requireNonNull(holdId, "holdId");
        while (true) {
            State current = state.get();
            if (current.status() != BookItemStatus.RESERVED || !holdId.equals(current.holdId())) {
                return false;
            }
            if (state.compareAndSet(current, new State(BookItemStatus.CHECKED_OUT, null))) {
                return true;
            }
        }
    }

    public boolean markReturnedAvailable() {
        while (true) {
            State current = state.get();
            if (current.status() != BookItemStatus.CHECKED_OUT) {
                return false;
            }
            if (state.compareAndSet(current, new State(BookItemStatus.AVAILABLE, null))) {
                return true;
            }
        }
    }

    public boolean markReturnedReservedForHold(String holdId) {
        Objects.requireNonNull(holdId, "holdId");
        while (true) {
            State current = state.get();
            if (current.status() != BookItemStatus.CHECKED_OUT) {
                return false;
            }
            if (state.compareAndSet(current, new State(BookItemStatus.RESERVED, holdId))) {
                return true;
            }
        }
    }

    public boolean markRemovedIfAvailable() {
        while (true) {
            State current = state.get();
            if (current.status() != BookItemStatus.AVAILABLE) {
                return false;
            }
            if (state.compareAndSet(current, new State(BookItemStatus.REMOVED, null))) {
                return true;
            }
        }
    }

    public boolean isAvailable() {
        return getStatus() == BookItemStatus.AVAILABLE;
    }

    public BookItemStatus getStatus() {
        return state.get().status();
    }

    public String getReservedHoldId() {
        return state.get().holdId();
    }

    public String getBarcode() {
        return barcode;
    }

    public String getBookId() {
        return bookId;
    }

    private record State(BookItemStatus status, String holdId) {
    }
}
