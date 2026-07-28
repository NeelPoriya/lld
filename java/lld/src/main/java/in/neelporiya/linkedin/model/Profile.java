package in.neelporiya.linkedin.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * // DESIGN PATTERN: Builder — a professional profile has many optional parts, so the builder keeps
 * tests and demos readable without telescoping constructors.
 */
public class Profile {

    private final String headline;
    private final List<Experience> experiences;
    private final List<Education> education;
    private final Set<String> skills;

    private Profile(Builder builder) {
        this.headline = Objects.requireNonNull(builder.headline, "headline");
        this.experiences = List.copyOf(builder.experiences);
        this.education = List.copyOf(builder.education);
        this.skills = Set.copyOf(builder.skills);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHeadline() {
        return headline;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public List<Education> getEducation() {
        return education;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public boolean hasSkill(String skill) {
        return skills.stream().anyMatch(s -> s.equalsIgnoreCase(skill));
    }

    public static class Builder {
        private String headline = "";
        private final List<Experience> experiences = new ArrayList<>();
        private final List<Education> education = new ArrayList<>();
        private final Set<String> skills = new LinkedHashSet<>();

        public Builder headline(String headline) {
            this.headline = headline;
            return this;
        }

        public Builder addExperience(Experience experience) {
            experiences.add(experience);
            return this;
        }

        public Builder addEducation(Education item) {
            education.add(item);
            return this;
        }

        public Builder addSkill(String skill) {
            skills.add(skill);
            return this;
        }

        public Profile build() {
            return new Profile(this);
        }
    }
}
