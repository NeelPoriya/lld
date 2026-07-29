package in.neelporiya.facebooksearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {

    private SearchService search;

    private static Instant at(int hour) {
        return Instant.EPOCH.plus(Duration.ofHours(hour));
    }

    @BeforeEach
    void setUp() {
        search = new SearchService(); // default popularity ranking
        search.index(new SearchDocument("u1", "John Smith", EntityType.USER, 500, at(3)));
        search.index(new SearchDocument("u2", "Johnny Appleseed", EntityType.USER, 900, at(1)));
        search.index(new SearchDocument("p1", "John's Page", EntityType.PAGE, 1000, at(2)));
    }

    private static List<String> ids(List<SearchDocument> docs) {
        return docs.stream().map(SearchDocument::id).collect(Collectors.toList());
    }

    @Test
    void typeaheadMatchesAnyWordPrefixRankedByPopularity() {
        // "joh" prefixes john (u1), johnny (u2), john (p1). Popularity: p1(1000) > u2(900) > u1(500).
        assertEquals(List.of("p1", "u2", "u1"), ids(search.typeahead("joh", 10)));
    }

    @Test
    void typeaheadHonoursLimit() {
        assertEquals(List.of("p1", "u2"), ids(search.typeahead("joh", 2)));
    }

    @Test
    void typeaheadOnALaterWordPrefix() {
        assertEquals(List.of("u1"), ids(search.typeahead("smi", 10)));
    }

    @Test
    void blankPrefixReturnsNothing() {
        assertTrue(search.typeahead("   ", 10).isEmpty());
    }

    @Test
    void fullTextSearchIsAndOverWords() {
        // "johnny" is not the word "john", so only u1 matches "john smith".
        assertEquals(List.of("u1"), ids(search.search("john smith", 10)));
    }

    @Test
    void fullTextSearchRanksByPopularity() {
        // token "john" appears in u1 and p1 (not u2's "johnny"). Popularity: p1 > u1.
        assertEquals(List.of("p1", "u1"), ids(search.search("john", 10)));
    }

    @Test
    void typeFilterRestrictsResults() {
        assertEquals(List.of("u1"), ids(search.search("john", EntityType.USER, 10)));
        assertEquals(List.of("p1"), ids(search.search("john", EntityType.PAGE, 10)));
    }

    @Test
    void rankingStrategyCanBeOverriddenPerQuery() {
        // u1 (createdAt hour 3) is newer than p1 (hour 2), so recency flips the popularity order.
        assertEquals(List.of("u1", "p1"), ids(search.search("john", null, 10, new RecencyRanking())));
    }

    @Test
    void searchWithNoMatchesIsEmpty() {
        assertTrue(search.search("nonexistent", 10).isEmpty());
    }
}
