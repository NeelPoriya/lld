package in.neelporiya.linkedin.search;

import in.neelporiya.linkedin.model.Job;

import java.util.Collection;
import java.util.List;

/** // DESIGN PATTERN: Strategy — job search can switch from in-memory title matching to an index. */
public interface JobSearchStrategy {
    List<Job> search(Collection<Job> jobs, String query);
}
