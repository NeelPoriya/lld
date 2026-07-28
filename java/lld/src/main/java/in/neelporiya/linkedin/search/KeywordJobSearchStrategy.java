package in.neelporiya.linkedin.search;

import in.neelporiya.linkedin.model.Job;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class KeywordJobSearchStrategy implements JobSearchStrategy {

    @Override
    public List<Job> search(Collection<Job> jobs, String query) {
        String needle = query.toLowerCase();
        return jobs.stream()
                .filter(job -> job.getTitle().toLowerCase().contains(needle)
                        || job.getDescription().toLowerCase().contains(needle)
                        || job.getCompany().getName().toLowerCase().contains(needle))
                .sorted(Comparator.comparing(Job::getTitle))
                .toList();
    }
}
