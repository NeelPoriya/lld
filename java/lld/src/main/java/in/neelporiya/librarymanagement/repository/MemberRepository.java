package in.neelporiya.librarymanagement.repository;

import in.neelporiya.librarymanagement.model.Member;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — member storage can later move from memory to a database. */
public class MemberRepository {

    private final Map<String, Member> members = new ConcurrentHashMap<>();

    public void save(Member member) {
        members.put(member.getId(), member);
    }

    public Optional<Member> findById(String memberId) {
        return Optional.ofNullable(members.get(memberId));
    }
}
