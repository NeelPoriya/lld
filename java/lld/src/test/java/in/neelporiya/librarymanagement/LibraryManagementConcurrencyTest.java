package in.neelporiya.librarymanagement;

import in.neelporiya.librarymanagement.exception.NoAvailableCopyException;
import in.neelporiya.librarymanagement.model.Book;
import in.neelporiya.librarymanagement.model.Loan;
import in.neelporiya.librarymanagement.model.Member;
import in.neelporiya.librarymanagement.service.LibraryService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: Many members race for one physical copy. The CAS in BookItem must allow exactly
 * one checkout and reject every other contender; no barcode can appear in two active loans.
 */
class LibraryManagementConcurrencyTest {

    @Test
    void manyMembersRacingForLastCopyProduceExactlyOneCheckout() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        LibraryService library = LibraryService.builder()
                .clock(MutableClock.atEpoch())
                .idGenerator(() -> "ID-" + ids.incrementAndGet())
                .maxBooksPerMember(1)
                .build();
        library.addBook(Book.builder()
                .id("b1")
                .isbn("ISBN-b1")
                .title("Concurrency in Practice")
                .author("Goetz")
                .subject("Programming")
                .build());
        library.addBookCopy("b1", "ONLY-COPY");

        int racers = 100;
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < racers; i++) {
            members.add(library.registerMember("M" + i));
        }

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(racers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger rejections = new AtomicInteger();
        Set<String> checkedOutBarcodes = ConcurrentHashMap.newKeySet();

        for (Member member : members) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    Loan loan = library.checkoutCopy(member.getId(), "ONLY-COPY");
                    successes.incrementAndGet();
                    checkedOutBarcodes.add(loan.getBarcode());
                } catch (NoAvailableCopyException expected) {
                    rejections.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGun.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertEquals(1, successes.get(), "exactly one member should atomically claim the last copy");
        assertEquals(racers - 1, rejections.get(), "everyone else must be rejected or offered a hold");
        assertEquals(Set.of("ONLY-COPY"), checkedOutBarcodes);
        assertEquals(0L, library.availableCopies("b1"));
    }
}
