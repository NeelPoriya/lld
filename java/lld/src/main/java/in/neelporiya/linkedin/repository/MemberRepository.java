package in.neelporiya.linkedin.repository;

import in.neelporiya.linkedin.model.Member;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — hides storage behind a tiny collection-like API. */
public class MemberRepository {

    private final Map<String, Member> membersById = new ConcurrentHashMap<>();

    public void save(Member member) {
        membersById.put(member.getId(), member);
    }

    public Member findById(String id) {
        return membersById.get(id);
    }

    public Collection<Member> findAll() {
        return membersById.values();
    }
}
