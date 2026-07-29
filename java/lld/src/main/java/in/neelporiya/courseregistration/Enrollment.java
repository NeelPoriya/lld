package in.neelporiya.courseregistration;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class Enrollment {

    private final String id;
    private final String studentId;
    private final String courseId;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    private volatile EnrollmentState state;

    public Enrollment(String id, String studentId, String courseId, EnrollmentState state, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.studentId = Objects.requireNonNull(studentId, "studentId");
        this.courseId = Objects.requireNonNull(courseId, "courseId");
        this.state = Objects.requireNonNull(state, "state");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = createdAt;
    }

    public synchronized void markEnrolled(Clock clock) {
        state = state.enroll();
        updatedAt = clock.instant();
    }

    public synchronized void markDropped(Clock clock) {
        state = state.drop();
        updatedAt = clock.instant();
    }

    public EnrollmentStatus getStatus() {
        return state.status();
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
