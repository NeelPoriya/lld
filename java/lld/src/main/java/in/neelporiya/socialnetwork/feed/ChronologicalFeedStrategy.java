package in.neelporiya.socialnetwork.feed;

import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ChronologicalFeedStrategy implements NewsFeedStrategy {

    @Override
    public List<Post> rank(User viewer, Collection<Post> candidatePosts) {
        return candidatePosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .toList();
    }
}
