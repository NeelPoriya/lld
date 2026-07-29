package in.neelporiya.facebooksearch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvertedIndexTest {

    @Test
    void matchAllIntersectsPostings() {
        InvertedIndex index = new InvertedIndex();
        index.add("john", "u1");
        index.add("smith", "u1");
        index.add("john", "u2");

        assertEquals(Set.of("u1", "u2"), index.matchAll(List.of("john")));
        assertEquals(Set.of("u1"), index.matchAll(List.of("john", "smith")), "AND semantics");
    }

    @Test
    void noCommonDocumentReturnsEmpty() {
        InvertedIndex index = new InvertedIndex();
        index.add("john", "u1");
        index.add("doe", "u2");
        assertTrue(index.matchAll(List.of("john", "doe")).isEmpty());
    }

    @Test
    void emptyTokenListReturnsEmpty() {
        InvertedIndex index = new InvertedIndex();
        index.add("john", "u1");
        assertTrue(index.matchAll(List.of()).isEmpty());
    }

    @Test
    void unknownTokenReturnsEmpty() {
        InvertedIndex index = new InvertedIndex();
        index.add("john", "u1");
        assertTrue(index.matchAll(List.of("nobody")).isEmpty());
    }
}
