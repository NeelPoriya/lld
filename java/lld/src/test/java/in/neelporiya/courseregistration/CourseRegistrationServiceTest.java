package in.neelporiya.courseregistration;

import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRegistrationServiceTest {

    private final AtomicInteger ids = new AtomicInteger();

    private RegistrationService service() {
        return RegistrationService.builder()
                .clock(MutableClock.atEpoch())
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .build();
    }

    private TimeSlot monday(int startHour, int endHour) {
        return new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }

    @Test
    void enrollReducesAvailableSeats() {
        RegistrationService service = service();
        Course course = service.createCourse("LLD", 2, Set.of(), monday(9, 10));
        Student student = service.registerStudent("Asha");

        Enrollment enrollment = service.enroll(student.getId(), course.getId());

        assertEquals(EnrollmentStatus.ENROLLED, enrollment.getStatus());
        assertEquals(1, service.availableSeats(course.getId()));
    }

    @Test
    void fullCourseWaitlistsNextStudent() {
        RegistrationService service = service();
        Course course = service.createCourse("Operating Systems", 1, Set.of(), monday(9, 10));
        Student first = service.registerStudent("Asha");
        Student second = service.registerStudent("Bala");

        service.enroll(first.getId(), course.getId());
        Enrollment waitlisted = service.enroll(second.getId(), course.getId());

        assertEquals(EnrollmentStatus.WAITLISTED, waitlisted.getStatus());
        assertEquals(0, service.availableSeats(course.getId()));
        assertEquals(1, service.waitlistSize(course.getId()));
    }

    @Test
    void droppingPromotesHeadOfWaitlistAndNotifiesObserver() {
        MutableClock clock = MutableClock.atEpoch();
        RegistrationService service = RegistrationService.builder()
                .clock(clock)
                .idGenerator(() -> "id-" + ids.incrementAndGet())
                .build();
        RecordingRegistrationNotificationListener listener = new RecordingRegistrationNotificationListener(clock);
        service.addNotificationListener(listener);
        Course course = service.createCourse("Databases", 1, Set.of(), monday(9, 10));
        Student first = service.registerStudent("Asha");
        Student second = service.registerStudent("Bala");

        service.enroll(first.getId(), course.getId());
        Enrollment waiting = service.enroll(second.getId(), course.getId());
        service.drop(first.getId(), course.getId());

        assertEquals(EnrollmentStatus.ENROLLED, waiting.getStatus());
        assertEquals(0, service.availableSeats(course.getId()));
        assertEquals(0, service.waitlistSize(course.getId()));
        assertTrue(listener.events().stream().anyMatch(event ->
                event.studentId().equals(second.getId())
                        && event.previousStatus() == EnrollmentStatus.WAITLISTED
                        && event.newStatus() == EnrollmentStatus.ENROLLED));
    }

    @Test
    void missingPrerequisiteIsRejected() {
        RegistrationService service = service();
        Course intro = service.createCourse("Intro", 10, Set.of(), monday(9, 10));
        Course advanced = service.createCourse("Advanced", 10, Set.of(intro.getId()), monday(10, 11));
        Student student = service.registerStudent("Asha");

        assertThrows(PrerequisiteNotMetException.class, () -> service.enroll(student.getId(), advanced.getId()));
    }

    @Test
    void scheduleConflictIsRejected() {
        RegistrationService service = service();
        Course morning = service.createCourse("Math", 10, Set.of(), monday(9, 11));
        Course overlapping = service.createCourse("Physics", 10, Set.of(), monday(10, 12));
        Student student = service.registerStudent("Asha");

        service.enroll(student.getId(), morning.getId());

        assertThrows(ScheduleConflictException.class, () -> service.enroll(student.getId(), overlapping.getId()));
    }
}
