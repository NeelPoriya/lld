package in.neelporiya.courseregistration;

public final class DroppedState implements EnrollmentState {

    @Override
    public EnrollmentStatus status() {
        return EnrollmentStatus.DROPPED;
    }

    @Override
    public EnrollmentState drop() {
        return this;
    }
}
