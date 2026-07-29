package in.neelporiya.facebooksearch;

import java.util.Collection;
import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — how a set of matching documents is ordered before we return the top
 * results. Popularity, recency, personalization, ML relevance — all swap in behind this one method.
 */
public interface RankingStrategy {

    /** Return the documents best-first. Must be deterministic (stable tie-break) for testability. */
    List<SearchDocument> rank(Collection<SearchDocument> documents);
}
