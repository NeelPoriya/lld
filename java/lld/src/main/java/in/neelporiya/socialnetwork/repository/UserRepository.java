package in.neelporiya.socialnetwork.repository;

import in.neelporiya.socialnetwork.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // DESIGN PATTERN: Repository — persistence is hidden behind a tiny collection-like API.
 */
public class UserRepository {

    private final Map<String, User> usersById = new ConcurrentHashMap<>();

    public void save(User user) {
        usersById.put(user.getId(), user);
    }

    public User findById(String id) {
        return usersById.get(id);
    }

    public Collection<User> findAll() {
        return usersById.values();
    }
}
