package in.neelporiya.stackoverflow.search;

import in.neelporiya.stackoverflow.model.Question;
import in.neelporiya.stackoverflow.model.Tag;

import java.util.Collection;
import java.util.List;

public class TagSearchStrategy implements QuestionSearchStrategy {

    private final Tag tag;

    public TagSearchStrategy(String tagName) {
        this.tag = new Tag(tagName);
    }

    @Override
    public List<Question> search(Collection<Question> questions) {
        return questions.stream()
                .filter(question -> question.getTags().contains(tag))
                .toList();
    }
}
