package in.neelporiya.courseregistration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StudentRepository {

    private final Map<String, Student> students = new ConcurrentHashMap<>();

    public void save(Student student) {
        students.put(student.getId(), student);
    }

    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    public List<Student> findAll() {
        return List.copyOf(students.values());
    }
}
