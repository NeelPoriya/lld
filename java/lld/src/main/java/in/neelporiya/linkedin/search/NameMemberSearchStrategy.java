package in.neelporiya.linkedin.search;

import in.neelporiya.linkedin.model.Member;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class NameMemberSearchStrategy implements MemberSearchStrategy {

    @Override
    public List<Member> search(Collection<Member> members, String query) {
        String needle = query.toLowerCase();
        return members.stream()
                .filter(member -> member.getName().toLowerCase().contains(needle))
                .sorted(Comparator.comparing(Member::getName))
                .toList();
    }
}
