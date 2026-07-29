package in.neelporiya.courseregistration;

import java.time.Instant;

public record RegistrationEvent(
        String enrollmentId,
        String studentId,
        String courseId,
        EnrollmentStatus previousStatus,
        EnrollmentStatus newStatus,
        Instant occurredAt) {
}
