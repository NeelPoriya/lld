package in.neelporiya.facebooksearch;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Newest first (by {@code createdAt}), ties broken by id for a deterministic order. */
public class RecencyRanking implements RankingStrategy {

    @Override
    public List<SearchDocument> rank(Collection<SearchDocument> documents) {
        return documents.stream()
                .sorted(Comparator.comparing(SearchDocument::createdAt).reversed()
                        .thenComparing(SearchDocument::id))
                .toList();
    }
}
