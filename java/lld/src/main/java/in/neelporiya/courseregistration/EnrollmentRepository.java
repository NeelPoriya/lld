package in.neelporiya.courseregistration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EnrollmentRepository {

    private final Map<String, Enrollment> byId = new ConcurrentHashMap<>();
    private final Map<String, Enrollment> byStudentAndCourse = new ConcurrentHashMap<>();

    public void save(Enrollment enrollment) {
        byId.put(enrollment.getId(), enrollment);
        byStudentAndCourse.put(key(enrollment.getStudentId(), enrollment.getCourseId()), enrollment);
    }

    public Optional<Enrollment> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<Enrollment> findByStudentAndCourse(String studentId, String courseId) {
        return Optional.ofNullable(byStudentAndCourse.get(key(studentId, courseId)));
    }

    public List<Enrollment> findByStudent(String studentId) {
        return byId.values().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .toList();
    }

    public List<Enrollment> findByCourse(String courseId) {
        return byId.values().stream()
                .filter(enrollment -> enrollment.getCourseId().equals(courseId))
                .toList();
    }

    private String key(String studentId, String courseId) {
        return studentId + "::" + courseId;
    }
}
