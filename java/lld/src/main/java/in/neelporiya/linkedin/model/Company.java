package in.neelporiya.linkedin.model;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Company {

    private final String id;
    private final String name;
    private final Set<String> jobIds = ConcurrentHashMap.newKeySet();

    public Company(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }

    public void addJob(Job job) {
        jobIds.add(job.getId());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getJobIds() {
        return Set.copyOf(jobIds);
    }
}
