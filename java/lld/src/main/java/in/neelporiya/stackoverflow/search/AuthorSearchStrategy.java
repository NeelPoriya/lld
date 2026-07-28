package in.neelporiya.stackoverflow.search;

import in.neelporiya.stackoverflow.model.Question;

import java.util.Collection;
import java.util.List;

public class AuthorSearchStrategy implements QuestionSearchStrategy {

    private final String authorId;

    public AuthorSearchStrategy(String authorId) {
        this.authorId = authorId;
    }

    @Override
    public List<Question> search(Collection<Question> questions) {
        return questions.stream()
                .filter(question -> question.getAuthor().getId().equals(authorId))
                .toList();
    }
}
