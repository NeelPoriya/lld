package in.neelporiya.librarymanagement.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** A library patron with an atomic count of active loans. */
public class Member {

    private final String id;
    private final String name;
    private final AtomicInteger activeLoanCount = new AtomicInteger();

    public Member(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public boolean tryAcquireBorrowSlot(int maxBooks) {
        while (true) {
            int current = activeLoanCount.get();
            if (current >= maxBooks) {
                return false;
            }
            if (activeLoanCount.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    public void releaseBorrowSlot() {
        activeLoanCount.updateAndGet(current -> Math.max(0, current - 1));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getActiveLoanCount() {
        return activeLoanCount.get();
    }
}
