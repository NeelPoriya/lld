package in.neelporiya.linkedin.feed;

import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Post;

import java.util.Collection;
import java.util.List;

/** // DESIGN PATTERN: Strategy — chronological today, relevance-ranking tomorrow. */
public interface NewsFeedStrategy {
    List<Post> rank(Member viewer, Collection<Post> posts);
}
