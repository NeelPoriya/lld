package in.neelporiya.socialnetwork.feed;

import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.User;

import java.util.Collection;
import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — ranking can move from chronological to relevance/edge-rank without
 * editing repositories, posts, or the facade.
 */
public interface NewsFeedStrategy {

    List<Post> rank(User viewer, Collection<Post> candidatePosts);
}
