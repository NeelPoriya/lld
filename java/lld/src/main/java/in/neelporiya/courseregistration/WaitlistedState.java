package in.neelporiya.courseregistration;

public final class WaitlistedState implements EnrollmentState {

    @Override
    public EnrollmentStatus status() {
        return EnrollmentStatus.WAITLISTED;
    }

    @Override
    public EnrollmentState enroll() {
        return new EnrolledState();
    }

    @Override
    public EnrollmentState drop() {
        return new DroppedState();
    }
}
