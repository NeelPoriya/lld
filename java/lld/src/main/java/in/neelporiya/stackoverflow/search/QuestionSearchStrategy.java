package in.neelporiya.stackoverflow.search;

import in.neelporiya.stackoverflow.model.Question;

import java.util.Collection;
import java.util.List;

/**
 * // DESIGN PATTERN: Strategy — a search dimension. New ways to search (by score, by date, full
 * text) are new classes; the service's {@code search} method never changes.
 */
public interface QuestionSearchStrategy {
    List<Question> search(Collection<Question> questions);
}
