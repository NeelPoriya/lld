package in.neelporiya.linkedin.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Job {

    private final String id;
    private final Company company;
    private final String title;
    private final String description;
    private final Instant postedAt;
    private final Set<String> applicantIds = ConcurrentHashMap.newKeySet();

    public Job(String id, Company company, String title, String description, Instant postedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.company = Objects.requireNonNull(company, "company");
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(description, "description");
        this.postedAt = Objects.requireNonNull(postedAt, "postedAt");
    }

    /** // CONCURRENCY: applying is idempotent per (member, job) via one atomic set add. */
    public boolean apply(Member member) {
        return applicantIds.add(member.getId());
    }

    public int applicationCount() {
        return applicantIds.size();
    }

    public boolean hasApplicant(Member member) {
        return applicantIds.contains(member.getId());
    }

    public String getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getPostedAt() {
        return postedAt;
    }
}
