package in.neelporiya.linkedin.search;

import in.neelporiya.linkedin.model.Member;

import java.util.Collection;
import java.util.List;

/** // DESIGN PATTERN: Strategy — each search dimension is independently replaceable/indexable. */
public interface MemberSearchStrategy {
    List<Member> search(Collection<Member> members, String query);
}
