package in.neelporiya.facebooksearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Splits free text into normalized (lowercase, alphanumeric) word tokens.
 *
 * <p>// INTERVIEW INSIGHT: tokenization is the shared front-door for BOTH indexes — the trie
 * (per-word prefixes) and the inverted index (whole words) must agree on what a "word" is, or a
 * document will be findable one way but not the other.
 */
final class Tokenizer {

    private Tokenizer() {
    }

    static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (String raw : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (!raw.isEmpty()) {
                tokens.add(raw);
            }
        }
        return tokens;
    }
}
