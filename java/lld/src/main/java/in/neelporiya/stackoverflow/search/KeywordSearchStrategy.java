package in.neelporiya.stackoverflow.search;

import in.neelporiya.stackoverflow.model.Question;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class KeywordSearchStrategy implements QuestionSearchStrategy {

    private final String keyword;

    public KeywordSearchStrategy(String keyword) {
        this.keyword = keyword.toLowerCase(Locale.ROOT);
    }

    @Override
    public List<Question> search(Collection<Question> questions) {
        return questions.stream()
                .filter(this::matches)
                .toList();
    }

    private boolean matches(Question question) {
        return question.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
                || question.getBody().toLowerCase(Locale.ROOT).contains(keyword);
    }
}
