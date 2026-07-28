package in.neelporiya.linkedin.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // INTERVIEW INSIGHT: endorsements are modeled as a set of endorsing member ids, not an integer
 * counter. The count is a projection of the set, so double-clicks cannot inflate it.
 */
public class Member {

    private final String id;
    private final String name;
    private final Profile profile;
    private final Set<String> connectionIds = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> endorsersBySkill = new ConcurrentHashMap<>();

    public Member(String id, String name, Profile profile) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    /** // CONCURRENCY: ConcurrentHashMap key-set makes duplicate relationship writes harmless. */
    public void addConnection(Member other) {
        connectionIds.add(other.getId());
    }

    public boolean isConnectedTo(Member other) {
        return connectionIds.contains(other.getId());
    }

    /**
     * // CONCURRENCY: computeIfAbsent creates one concurrent set per skill, and add(endorserId) is
     * atomic. Even many concurrent retries for the same (endorser, member, skill) return true once.
     */
    public boolean endorseSkill(Member endorser, String skill) {
        if (equals(endorser)) {
            throw new IllegalArgumentException("Members cannot endorse themselves");
        }
        if (!profile.hasSkill(skill)) {
            throw new IllegalArgumentException("Member does not list skill: " + skill);
        }
        String normalizedSkill = normalize(skill);
        return endorsersBySkill.computeIfAbsent(normalizedSkill, ignored -> ConcurrentHashMap.newKeySet())
                .add(endorser.getId());
    }

    public int endorsementCount(String skill) {
        return endorsersBySkill.getOrDefault(normalize(skill), Set.of()).size();
    }

    private static String normalize(String skill) {
        return skill.toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Profile getProfile() {
        return profile;
    }

    public Set<String> getConnectionIds() {
        return Set.copyOf(connectionIds);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Member other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
