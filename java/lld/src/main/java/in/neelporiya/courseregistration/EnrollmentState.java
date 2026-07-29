package in.neelporiya.courseregistration;

/**
 * // DESIGN PATTERN: State. Enrollment transition rules live behind this interface instead of
 * scattering status-switch statements through the service.
 */
public interface EnrollmentState {

    EnrollmentStatus status();

    default EnrollmentState enroll() {
        throw new IllegalStateException("Cannot enroll from " + status());
    }

    default EnrollmentState waitlist() {
        throw new IllegalStateException("Cannot waitlist from " + status());
    }

    default EnrollmentState drop() {
        throw new IllegalStateException("Cannot drop from " + status());
    }
}
