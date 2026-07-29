package in.neelporiya.facebooksearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A prefix tree powering typeahead. Every node along a word's path remembers the ids of documents
 * that contain a word with that prefix, so a lookup is O(prefix length) and returns every completion
 * immediately.
 *
 * <p>// INTERVIEW INSIGHT: storing doc ids at EVERY node (not just word ends) trades memory for speed
 * — "joh" → all docs with a word starting "joh" without walking the subtree. At scale you'd cap each
 * node to a precomputed top-k by score; mention that as the next optimization.
 *
 * <p>Not thread-safe on its own; the {@link SearchService} guards it with a read/write lock.
 */
public class Trie {

    private static final class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private final Set<String> docIds = new HashSet<>();
    }

    private final Node root = new Node();

    /** Associate {@code docId} with every prefix of {@code word}. */
    public void insert(String word, String docId) {
        Node node = root;
        for (char c : word.toLowerCase(Locale.ROOT).toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new Node());
            node.docIds.add(docId);
        }
    }

    /** @return ids of documents that contain a word starting with {@code prefix}. */
    public Set<String> searchPrefix(String prefix) {
        Node node = root;
        for (char c : prefix.toLowerCase(Locale.ROOT).toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                return Set.of();
            }
        }
        return new HashSet<>(node.docIds);
    }
}
