package in.neelporiya.facebooksearch;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Most popular first (friends/followers/likes), ties broken by id for a deterministic order. */
public class PopularityRanking implements RankingStrategy {

    @Override
    public List<SearchDocument> rank(Collection<SearchDocument> documents) {
        return documents.stream()
                .sorted(Comparator.comparingLong(SearchDocument::popularity).reversed()
                        .thenComparing(SearchDocument::id))
                .toList();
    }
}
