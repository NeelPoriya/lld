package in.neelporiya.courseregistration;

/**
 * // DESIGN PATTERN: Observer. Email, SMS, audit log and tests can subscribe to registration events
 * without the facade knowing which delivery channels exist.
 */
public interface RegistrationNotificationListener {

    void onEnrollmentChanged(Enrollment enrollment, EnrollmentStatus previousStatus, EnrollmentStatus newStatus);
}
