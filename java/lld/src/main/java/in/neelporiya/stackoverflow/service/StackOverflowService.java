package in.neelporiya.stackoverflow.service;

import in.neelporiya.stackoverflow.event.PostEventListener;
import in.neelporiya.stackoverflow.exception.UnauthorizedActionException;
import in.neelporiya.stackoverflow.model.AcceptResult;
import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Comment;
import in.neelporiya.stackoverflow.model.Post;
import in.neelporiya.stackoverflow.model.Question;
import in.neelporiya.stackoverflow.model.Tag;
import in.neelporiya.stackoverflow.model.User;
import in.neelporiya.stackoverflow.model.VoteChange;
import in.neelporiya.stackoverflow.model.VoteType;
import in.neelporiya.stackoverflow.reputation.DefaultReputationRules;
import in.neelporiya.stackoverflow.reputation.ReputationManager;
import in.neelporiya.stackoverflow.search.QuestionSearchStrategy;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * // DESIGN PATTERN: Facade — the single entry point clients use. It owns the in-memory
 * repositories, injects a {@link Clock} and an id generator (// TESTABILITY), and fans domain
 * events out to {@link PostEventListener}s (// DESIGN PATTERN: Observer).
 */
public class StackOverflowService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Question> questions = new ConcurrentHashMap<>();
    private final Map<String, Answer> answers = new ConcurrentHashMap<>();
    private final List<PostEventListener> listeners = new CopyOnWriteArrayList<>();

    private final Clock clock;
    private final Supplier<String> idGenerator;

    public StackOverflowService(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        // Reputation is just another observer; wiring it here keeps model free of reputation logic.
        this.listeners.add(new ReputationManager(new DefaultReputationRules()));
    }

    /** Production-style default: system clock + random ids. */
    public static StackOverflowService createDefault() {
        return new StackOverflowService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(PostEventListener listener) {
        listeners.add(listener);
    }

    public User registerUser(String name) {
        User user = new User(idGenerator.get(), name);
        users.put(user.getId(), user);
        return user;
    }

    public Question postQuestion(User author, String title, String body, Set<String> tagNames) {
        Set<Tag> tags = tagNames.stream().map(Tag::new).collect(Collectors.toUnmodifiableSet());
        Question question = new Question(idGenerator.get(), author, title, body, tags, clock.instant());
        questions.put(question.getId(), question);
        return question;
    }

    public Answer postAnswer(User author, Question question, String body) {
        Answer answer = new Answer(idGenerator.get(), author, question, body, clock.instant());
        question.addAnswer(answer);
        answers.put(answer.getId(), answer);
        return answer;
    }

    public Comment addComment(User author, Post post, String text) {
        Comment comment = new Comment(idGenerator.get(), author, text, clock.instant());
        post.addComment(comment);
        return comment;
    }

    /**
     * Cast (or change) a vote.
     *
     * <p>// INTERVIEW INSIGHT: you cannot vote on your own post — a real rule that also prevents
     * trivial reputation farming. The atomic vote happens inside {@link Post#castVote}; we then fan
     * the resulting {@link VoteChange} out to observers so reputation updates.
     */
    public VoteChange vote(User voter, Post post, VoteType type) {
        if (post.getAuthor().equals(voter)) {
            throw new UnauthorizedActionException("You cannot vote on your own post");
        }
        VoteChange change = post.castVote(voter, type);
        if (!change.isNoOp()) {
            listeners.forEach(listener -> listener.onVote(post, post.getAuthor(), change));
        }
        return change;
    }

    public VoteChange retractVote(User voter, Post post) {
        VoteChange change = post.retractVote(voter);
        if (!change.isNoOp()) {
            listeners.forEach(listener -> listener.onVote(post, post.getAuthor(), change));
        }
        return change;
    }

    public void acceptAnswer(User byUser, Answer answer) {
        Question question = answer.getQuestion();
        AcceptResult result = question.accept(answer, byUser);
        if (result.changed()) {
            listeners.forEach(listener -> listener.onAnswerAccepted(answer, result.previouslyAccepted()));
        }
    }

    public List<Question> search(QuestionSearchStrategy strategy) {
        return strategy.search(questions.values());
    }

    public User getUser(String id) {
        return users.get(id);
    }
}
