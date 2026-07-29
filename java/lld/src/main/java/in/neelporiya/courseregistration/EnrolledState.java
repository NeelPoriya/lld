package in.neelporiya.courseregistration;

public final class EnrolledState implements EnrollmentState {

    @Override
    public EnrollmentStatus status() {
        return EnrollmentStatus.ENROLLED;
    }

    @Override
    public EnrollmentState drop() {
        return new DroppedState();
    }
}
