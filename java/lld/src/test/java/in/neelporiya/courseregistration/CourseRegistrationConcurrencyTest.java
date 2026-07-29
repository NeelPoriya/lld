package in.neelporiya.courseregistration;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * // CONCURRENCY: Releases many registration attempts at once and proves the atomic seat counter
 * never over-enrolls the course.
 */
class CourseRegistrationConcurrencyTest {

    @Test
    void concurrentEnrollmentFillsCapacityAndWaitlistsTheRest() throws InterruptedException {
        AtomicInteger ids = new AtomicInteger();
        RegistrationService service = RegistrationService.builder()
                .clock(MutableClock.atEpoch())
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .build();
        int capacity = 10;
        int students = 200;
        Course course = service.createCourse("Distributed Systems", capacity, Set.of(),
                new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        List<Student> roster = java.util.stream.IntStream.range(0, students)
                .mapToObj(i -> service.registerStudent("student-" + i))
                .toList();

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(students);

        for (Student student : roster) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    service.enroll(student.getId(), course.getId());
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

        long enrolled = service.enrollmentsForCourse(course.getId()).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .count();
        long waitlisted = service.enrollmentsForCourse(course.getId()).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.WAITLISTED)
                .count();

        assertEquals(capacity, enrolled, "exactly capacity students should be enrolled");
        assertEquals(students - capacity, waitlisted, "everyone else should be waitlisted");
        assertEquals(0, service.availableSeats(course.getId()), "course must report full");
        assertEquals(students - capacity, service.waitlistSize(course.getId()));
    }
}
