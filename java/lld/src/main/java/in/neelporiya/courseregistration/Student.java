package in.neelporiya.courseregistration;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Student {

    private final String id;
    private final String name;
    private final Set<String> completedCourseIds = ConcurrentHashMap.newKeySet();

    public Student(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public void markCompleted(String courseId) {
        completedCourseIds.add(Objects.requireNonNull(courseId, "courseId"));
    }

    public boolean hasCompleted(String courseId) {
        return completedCourseIds.contains(courseId);
    }

    public Set<String> getCompletedCourseIds() {
        return Set.copyOf(completedCourseIds);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
