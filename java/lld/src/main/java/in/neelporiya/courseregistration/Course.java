package in.neelporiya.courseregistration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class Course {

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<String> prerequisiteCourseIds;
    private final TimeSlot timeSlot;
    private final AtomicInteger availableSeats;
    private final Deque<String> waitlistedStudentIds = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Course(String id, String name, int capacity, Set<String> prerequisiteCourseIds, TimeSlot timeSlot) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.capacity = capacity;
        this.prerequisiteCourseIds = Set.copyOf(prerequisiteCourseIds);
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.availableSeats = new AtomicInteger(capacity);
    }

    /**
     * Atomically claims one seat if any are still available.
     *
     * <p>// CONCURRENCY: This CAS loop is the registration equivalent of the ParkingSpot CAS. The
     * read ("seats left?") and write ("take one") are fused, so 200 threads racing for 10 seats can
     * produce only 10 winners.
     */
    public boolean tryClaimSeat() {
        while (true) {
            int current = availableSeats.get();
            if (current == 0) {
                return false;
            }
            if (availableSeats.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }

    public void releaseSeat() {
        int updated = availableSeats.incrementAndGet();
        if (updated > capacity) {
            availableSeats.decrementAndGet();
            throw new IllegalStateException("available seats cannot exceed capacity");
        }
    }

    public <T> T withRegistrationLock(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    void enqueueWaitlistedStudent(String studentId) {
        waitlistedStudentIds.addLast(studentId);
    }

    boolean removeWaitlistedStudent(String studentId) {
        return waitlistedStudentIds.remove(studentId);
    }

    Deque<String> waitlistQueue() {
        return waitlistedStudentIds;
    }

    public List<String> getWaitlistedStudentIds() {
        return withRegistrationLock(() -> List.copyOf(waitlistedStudentIds));
    }

    public int getAvailableSeats() {
        return availableSeats.get();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<String> getPrerequisiteCourseIds() {
        return prerequisiteCourseIds;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }
}
