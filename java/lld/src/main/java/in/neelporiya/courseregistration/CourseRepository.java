package in.neelporiya.courseregistration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // DESIGN PATTERN: Repository — storage is deliberately hidden behind a small collection-like API.
 */
public class CourseRepository {

    private final Map<String, Course> courses = new ConcurrentHashMap<>();

    public void save(Course course) {
        courses.put(course.getId(), course);
    }

    public Optional<Course> findById(String id) {
        return Optional.ofNullable(courses.get(id));
    }

    public List<Course> findAll() {
        return List.copyOf(courses.values());
    }
}
