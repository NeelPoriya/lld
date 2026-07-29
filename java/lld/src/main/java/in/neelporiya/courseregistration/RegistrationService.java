package in.neelporiya.courseregistration;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade. Interview clients talk to this one class while repositories, states,
 * observers and waitlist strategies remain internal collaborators.
 *
 * <p>// TESTABILITY: Time and ids are injected, so tests never sleep and never depend on UUID shape.
 */
public class RegistrationService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistOrderingStrategy waitlistOrderingStrategy;
    private final List<RegistrationNotificationListener> listeners = new CopyOnWriteArrayList<>();
    private final Clock clock;
    private final Supplier<String> idGenerator;

    private RegistrationService(Builder builder) {
        this.courseRepository = builder.courseRepository;
        this.studentRepository = builder.studentRepository;
        this.enrollmentRepository = builder.enrollmentRepository;
        this.waitlistOrderingStrategy = builder.waitlistOrderingStrategy;
        this.clock = builder.clock;
        this.idGenerator = builder.idGenerator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RegistrationService createDefault() {
        return builder().build();
    }

    public void addNotificationListener(RegistrationNotificationListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public Student registerStudent(String name) {
        Student student = new Student(idGenerator.get(), name);
        studentRepository.save(student);
        return student;
    }

    public Course createCourse(String name, int capacity, Set<String> prerequisiteCourseIds, TimeSlot timeSlot) {
        Course course = new Course(idGenerator.get(), name, capacity, prerequisiteCourseIds, timeSlot);
        courseRepository.save(course);
        return course;
    }

    public void markCourseCompleted(String studentId, String courseId) {
        requireStudent(studentId).markCompleted(courseId);
    }

    public Enrollment enroll(String studentId, String courseId) {
        Student student = requireStudent(studentId);
        Course course = requireCourse(courseId);
        validatePrerequisites(student, course);
        validateSchedule(studentId, course);

        return course.withRegistrationLock(() -> {
            Enrollment existing = enrollmentRepository.findByStudentAndCourse(studentId, courseId).orElse(null);
            if (existing != null && existing.getStatus() != EnrollmentStatus.DROPPED) {
                return existing;
            }

            if (course.tryClaimSeat()) {
                Enrollment enrollment = new Enrollment(idGenerator.get(), studentId, courseId,
                        new EnrolledState(), clock.instant());
                enrollmentRepository.save(enrollment);
                notifyListeners(enrollment, null, EnrollmentStatus.ENROLLED);
                return enrollment;
            }

            Enrollment enrollment = new Enrollment(idGenerator.get(), studentId, courseId,
                    new WaitlistedState(), clock.instant());
            course.enqueueWaitlistedStudent(studentId);
            enrollmentRepository.save(enrollment);
            notifyListeners(enrollment, null, EnrollmentStatus.WAITLISTED);
            return enrollment;
        });
    }

    public Enrollment drop(String studentId, String courseId) {
        Student student = requireStudent(studentId);
        Course course = requireCourse(courseId);
        Objects.requireNonNull(student, "student");

        return course.withRegistrationLock(() -> {
            Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(studentId, courseId)
                    .orElseThrow(() -> new RegistrationException("No enrollment for student " + studentId));
            EnrollmentStatus previous = enrollment.getStatus();
            if (previous == EnrollmentStatus.DROPPED) {
                return enrollment;
            }

            enrollment.markDropped(clock);
            if (previous == EnrollmentStatus.WAITLISTED) {
                course.removeWaitlistedStudent(studentId);
            } else {
                course.releaseSeat();
                promoteHeadOfWaitlist(course);
            }
            notifyListeners(enrollment, previous, EnrollmentStatus.DROPPED);
            return enrollment;
        });
    }

    public int availableSeats(String courseId) {
        return requireCourse(courseId).getAvailableSeats();
    }

    public int waitlistSize(String courseId) {
        return requireCourse(courseId).getWaitlistedStudentIds().size();
    }

    public List<Enrollment> enrollmentsForCourse(String courseId) {
        requireCourse(courseId);
        return enrollmentRepository.findByCourse(courseId);
    }

    public List<Enrollment> enrollmentsForStudent(String studentId) {
        requireStudent(studentId);
        return enrollmentRepository.findByStudent(studentId);
    }

    private void promoteHeadOfWaitlist(Course course) {
        while (course.getAvailableSeats() > 0 && !course.waitlistQueue().isEmpty()) {
            String nextStudentId = waitlistOrderingStrategy.selectNextStudent(course.waitlistQueue());
            Enrollment next = enrollmentRepository.findByStudentAndCourse(nextStudentId, course.getId()).orElse(null);
            if (next == null || next.getStatus() != EnrollmentStatus.WAITLISTED) {
                continue;
            }

            Student student = requireStudent(nextStudentId);
            if (!isEligibleForPromotion(student, course)) {
                next.markDropped(clock);
                notifyListeners(next, EnrollmentStatus.WAITLISTED, EnrollmentStatus.DROPPED);
                continue;
            }

            if (course.tryClaimSeat()) {
                // CONCURRENCY: Drop, seat release, FIFO pop and promotion all happen while the
                // course lock is held; the CAS counter remains the authoritative capacity guard.
                next.markEnrolled(clock);
                notifyListeners(next, EnrollmentStatus.WAITLISTED, EnrollmentStatus.ENROLLED);
                return;
            }
        }
    }

    private boolean isEligibleForPromotion(Student student, Course course) {
        return hasPrerequisites(student, course) && hasNoScheduleConflict(student.getId(), course);
    }

    private void validatePrerequisites(Student student, Course course) {
        if (!hasPrerequisites(student, course)) {
            throw new PrerequisiteNotMetException("Student " + student.getId()
                    + " has not completed prerequisites for " + course.getId());
        }
    }

    private boolean hasPrerequisites(Student student, Course course) {
        return course.getPrerequisiteCourseIds().stream().allMatch(student::hasCompleted);
    }

    private void validateSchedule(String studentId, Course candidate) {
        if (!hasNoScheduleConflict(studentId, candidate)) {
            throw new ScheduleConflictException("Student " + studentId + " has a schedule conflict");
        }
    }

    private boolean hasNoScheduleConflict(String studentId, Course candidate) {
        return enrollmentRepository.findByStudent(studentId).stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .filter(enrollment -> !enrollment.getCourseId().equals(candidate.getId()))
                .map(enrollment -> requireCourse(enrollment.getCourseId()))
                .noneMatch(course -> course.getTimeSlot().overlaps(candidate.getTimeSlot()));
    }

    private Student requireStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RegistrationException("No student with id " + studentId));
    }

    private Course requireCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RegistrationException("No course with id " + courseId));
    }

    private void notifyListeners(Enrollment enrollment, EnrollmentStatus previous, EnrollmentStatus next) {
        listeners.forEach(listener -> listener.onEnrollmentChanged(enrollment, previous, next));
    }

    public static class Builder {
        private CourseRepository courseRepository = new CourseRepository();
        private StudentRepository studentRepository = new StudentRepository();
        private EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
        private WaitlistOrderingStrategy waitlistOrderingStrategy = new FifoWaitlistOrderingStrategy();
        private Clock clock = Clock.systemUTC();
        private Supplier<String> idGenerator = () -> UUID.randomUUID().toString();

        public Builder courseRepository(CourseRepository courseRepository) {
            this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository");
            return this;
        }

        public Builder studentRepository(StudentRepository studentRepository) {
            this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository");
            return this;
        }

        public Builder enrollmentRepository(EnrollmentRepository enrollmentRepository) {
            this.enrollmentRepository = Objects.requireNonNull(enrollmentRepository, "enrollmentRepository");
            return this;
        }

        public Builder waitlistOrderingStrategy(WaitlistOrderingStrategy waitlistOrderingStrategy) {
            this.waitlistOrderingStrategy = Objects.requireNonNull(waitlistOrderingStrategy, "waitlistOrderingStrategy");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public Builder idGenerator(Supplier<String> idGenerator) {
            this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
            return this;
        }

        public RegistrationService build() {
            return new RegistrationService(this);
        }
    }
}
