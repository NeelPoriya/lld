package in.neelporiya.linkedin.search;

import in.neelporiya.linkedin.model.Member;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SkillMemberSearchStrategy implements MemberSearchStrategy {

    @Override
    public List<Member> search(Collection<Member> members, String query) {
        return members.stream()
                .filter(member -> member.getProfile().hasSkill(query))
                .sorted(Comparator.comparing(Member::getName))
                .toList();
    }
}
