package in.neelporiya.facebooksearch;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrieTest {

    @Test
    void prefixReturnsAllDocsWithThatWordPrefix() {
        Trie trie = new Trie();
        trie.insert("john", "u1");
        trie.insert("johnny", "u2");
        trie.insert("jane", "u3");

        assertEquals(Set.of("u1", "u2"), trie.searchPrefix("joh"));
        assertEquals(Set.of("u1", "u2", "u3"), trie.searchPrefix("j"));
        assertEquals(Set.of("u2"), trie.searchPrefix("johnn"));
    }

    @Test
    void prefixIsCaseInsensitive() {
        Trie trie = new Trie();
        trie.insert("Smith", "u1");
        assertEquals(Set.of("u1"), trie.searchPrefix("SMI"));
        assertEquals(Set.of("u1"), trie.searchPrefix("smi"));
    }

    @Test
    void unknownPrefixReturnsEmpty() {
        Trie trie = new Trie();
        trie.insert("john", "u1");
        assertTrue(trie.searchPrefix("xyz").isEmpty());
        assertTrue(trie.searchPrefix("johx").isEmpty());
    }

    @Test
    void sameDocInsertedTwiceIsNotDuplicated() {
        Trie trie = new Trie();
        trie.insert("john", "u1");
        trie.insert("john", "u1");
        assertEquals(Set.of("u1"), trie.searchPrefix("john"));
    }
}
