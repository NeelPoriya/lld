package in.neelporiya.linkedin.model;

import java.time.Instant;

public record JobApplication(String id, Member applicant, Job job, Instant createdAt) {
}
