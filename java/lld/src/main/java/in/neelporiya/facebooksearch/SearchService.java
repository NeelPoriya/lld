package in.neelporiya.facebooksearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * // DESIGN PATTERN: Facade — one search API over two indexes (a {@link Trie} for typeahead and an
 * {@link InvertedIndex} for full-text) and a pluggable {@link RankingStrategy}.
 *
 * <p>// CONCURRENCY: search is overwhelmingly reads with occasional index writes, so a
 * {@link ReentrantReadWriteLock} lets many searches run in parallel while an {@code index} call takes
 * the write lock exclusively. Both indexes are mutated together under that write lock, so a document
 * never appears in one index but not the other.
 */
public class SearchService {

    private final Trie trie = new Trie();
    private final InvertedIndex invertedIndex = new InvertedIndex();
    private final Map<String, SearchDocument> documents = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final RankingStrategy defaultRanking;

    public SearchService() {
        this(new PopularityRanking());
    }

    public SearchService(RankingStrategy defaultRanking) {
        this.defaultRanking = Objects.requireNonNull(defaultRanking, "defaultRanking");
    }

    /** Add or replace a document in both indexes atomically. */
    public void index(SearchDocument document) {
        lock.writeLock().lock();
        try {
            documents.put(document.id(), document);
            for (String token : Tokenizer.tokenize(document.text())) {
                trie.insert(token, document.id());
                invertedIndex.add(token, document.id());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<SearchDocument> typeahead(String prefix, int limit) {
        return typeahead(prefix, limit, defaultRanking);
    }

    /** Top {@code limit} documents that have a word starting with {@code prefix}, ranked. */
    public List<SearchDocument> typeahead(String prefix, int limit, RankingStrategy ranking) {
        if (prefix.isBlank()) {
            return List.of();
        }
        lock.readLock().lock();
        try {
            return rankAndLimit(resolve(trie.searchPrefix(prefix.trim()), null), ranking, limit);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<SearchDocument> search(String query, int limit) {
        return search(query, null, limit, defaultRanking);
    }

    public List<SearchDocument> search(String query, EntityType typeFilter, int limit) {
        return search(query, typeFilter, limit, defaultRanking);
    }

    /**
     * Full-text AND search: documents containing every query word, optionally filtered to one
     * {@link EntityType}, ranked and truncated to {@code limit}.
     */
    public List<SearchDocument> search(String query, EntityType typeFilter, int limit, RankingStrategy ranking) {
        lock.readLock().lock();
        try {
            Set<String> ids = invertedIndex.matchAll(Tokenizer.tokenize(query));
            return rankAndLimit(resolve(ids, typeFilter), ranking, limit);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        return documents.size();
    }

    private List<SearchDocument> resolve(Set<String> ids, EntityType typeFilter) {
        List<SearchDocument> docs = new ArrayList<>();
        for (String id : ids) {
            SearchDocument doc = documents.get(id);
            if (doc != null && (typeFilter == null || doc.type() == typeFilter)) {
                docs.add(doc);
            }
        }
        return docs;
    }

    private static List<SearchDocument> rankAndLimit(List<SearchDocument> docs, RankingStrategy ranking, int limit) {
        List<SearchDocument> ranked = ranking.rank(docs);
        return limit < ranked.size() ? List.copyOf(ranked.subList(0, limit)) : ranked;
    }
}
