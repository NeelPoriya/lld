package in.neelporiya.linkedin.feed;

import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Post;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ChronologicalFeedStrategy implements NewsFeedStrategy {

    @Override
    public List<Post> rank(Member viewer, Collection<Post> posts) {
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .toList();
    }
}
