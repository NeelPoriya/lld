package in.neelporiya.courseregistration;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * // TESTABILITY: A tiny in-memory observer lets tests assert that promotions notify subscribers
 * without mocking frameworks.
 */
public class RecordingRegistrationNotificationListener implements RegistrationNotificationListener {

    private final Clock clock;
    private final List<RegistrationEvent> events = new CopyOnWriteArrayList<>();

    public RecordingRegistrationNotificationListener(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void onEnrollmentChanged(Enrollment enrollment, EnrollmentStatus previousStatus, EnrollmentStatus newStatus) {
        events.add(new RegistrationEvent(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                previousStatus,
                newStatus,
                clock.instant()));
    }

    public List<RegistrationEvent> events() {
        return List.copyOf(events);
    }
}
