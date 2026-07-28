package in.neelporiya.librarymanagement.repository;

import in.neelporiya.librarymanagement.model.Hold;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** FIFO hold queues by book. */
public class HoldRepository {

    private final Map<String, Hold> holdsById = new ConcurrentHashMap<>();
    private final Map<String, Queue<Hold>> holdsByBook = new ConcurrentHashMap<>();

    public void save(Hold hold) {
        holdsById.put(hold.getId(), hold);
        holdsByBook.computeIfAbsent(hold.getBookId(), ignored -> new ConcurrentLinkedQueue<>()).add(hold);
    }

    public Optional<Hold> findById(String holdId) {
        return Optional.ofNullable(holdsById.get(holdId));
    }

    public Optional<Hold> findNotifiedHold(String memberId, String bookId) {
        return holdsById.values().stream()
                .filter(Hold::isNotified)
                .filter(hold -> hold.getMemberId().equals(memberId))
                .filter(hold -> hold.getBookId().equals(bookId))
                .findFirst();
    }

    public Hold pollNextWaiting(String bookId) {
        Queue<Hold> queue = holdsByBook.get(bookId);
        if (queue == null) {
            return null;
        }
        Hold hold;
        while ((hold = queue.poll()) != null) {
            if (holdsById.containsKey(hold.getId())) {
                return hold;
            }
        }
        return null;
    }

    public boolean remove(String holdId) {
        return holdsById.remove(holdId) != null;
    }
}
