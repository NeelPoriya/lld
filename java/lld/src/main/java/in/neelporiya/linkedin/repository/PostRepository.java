package in.neelporiya.linkedin.repository;

import in.neelporiya.linkedin.model.Post;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** // DESIGN PATTERN: Repository — post storage and feed retrieval are not baked into Post. */
public class PostRepository {

    private final Map<String, Post> postsById = new ConcurrentHashMap<>();

    public void save(Post post) {
        postsById.put(post.getId(), post);
    }

    public Post findById(String id) {
        return postsById.get(id);
    }

    public Collection<Post> findAll() {
        return postsById.values();
    }

    public List<Post> findByAuthorIds(Set<String> authorIds) {
        return postsById.values().stream()
                .filter(post -> authorIds.contains(post.getAuthor().getId()))
                .toList();
    }
}
