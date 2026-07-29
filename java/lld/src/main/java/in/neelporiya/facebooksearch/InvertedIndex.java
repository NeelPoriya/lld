package in.neelporiya.facebooksearch;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * token → set of document ids. Full-text queries intersect the postings of each query token (AND
 * semantics): a document matches only if it contains every query word.
 *
 * <p>// INTERVIEW INSIGHT: intersecting the SMALLEST posting list first is the standard speed-up; for
 * this LLD we keep it simple and intersect in order.
 */
public class InvertedIndex {

    private final Map<String, Set<String>> postings = new ConcurrentHashMap<>();

    public void add(String token, String docId) {
        postings.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet()).add(docId);
    }

    /** Documents containing ALL of the given tokens. Empty tokens → empty result. */
    public Set<String> matchAll(List<String> tokens) {
        if (tokens.isEmpty()) {
            return Set.of();
        }
        Set<String> result = null;
        for (String token : tokens) {
            Set<String> docs = postings.getOrDefault(token, Set.of());
            if (result == null) {
                result = new HashSet<>(docs);
            } else {
                result.retainAll(docs);
            }
            if (result.isEmpty()) {
                return Set.of();
            }
        }
        return result;
    }
}
