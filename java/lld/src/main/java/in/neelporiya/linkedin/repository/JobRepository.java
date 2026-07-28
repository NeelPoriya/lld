package in.neelporiya.linkedin.repository;

import in.neelporiya.linkedin.model.Job;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — keeps job indexing separate from the service facade. */
public class JobRepository {

    private final Map<String, Job> jobsById = new ConcurrentHashMap<>();

    public void save(Job job) {
        jobsById.put(job.getId(), job);
    }

    public Job findById(String id) {
        return jobsById.get(id);
    }

    public Collection<Job> findAll() {
        return jobsById.values();
    }
}
