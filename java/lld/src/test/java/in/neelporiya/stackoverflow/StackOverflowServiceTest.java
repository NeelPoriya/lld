package in.neelporiya.stackoverflow;

import in.neelporiya.stackoverflow.exception.UnauthorizedActionException;
import in.neelporiya.stackoverflow.model.Answer;
import in.neelporiya.stackoverflow.model.Question;
import in.neelporiya.stackoverflow.model.User;
import in.neelporiya.stackoverflow.model.VoteChange;
import in.neelporiya.stackoverflow.model.VoteType;
import in.neelporiya.stackoverflow.search.AuthorSearchStrategy;
import in.neelporiya.stackoverflow.search.KeywordSearchStrategy;
import in.neelporiya.stackoverflow.search.TagSearchStrategy;
import in.neelporiya.stackoverflow.service.StackOverflowService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackOverflowServiceTest {

    private StackOverflowService service;
    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        service = new StackOverflowService(MutableClock.atEpoch(), () -> "id-" + seq.incrementAndGet());
        alice = service.registerUser("alice");
        bob = service.registerUser("bob");
        carol = service.registerUser("carol");
    }

    private Answer answerFromBob() {
        Question q = service.postQuestion(alice, "How to CAS?", "explain compareAndSet", Set.of("java"));
        return service.postAnswer(bob, q, "Use AtomicReference.compareAndSet");
    }

    @Test
    void upvotingAnswerRaisesScoreAndAuthorReputation() {
        Answer answer = answerFromBob();
        service.vote(carol, answer, VoteType.UP);

        assertEquals(1, answer.getScore());
        assertEquals(10, bob.getReputation()); // answer upvote = +10
    }

    @Test
    void upvotingQuestionGivesFivePoints() {
        Question q = service.postQuestion(alice, "title", "body", Set.of("java"));
        service.vote(bob, q, VoteType.UP);

        assertEquals(1, q.getScore());
        assertEquals(5, alice.getReputation());
    }

    @Test
    void cannotVoteOnOwnPost() {
        Answer answer = answerFromBob();
        assertThrows(UnauthorizedActionException.class, () -> service.vote(bob, answer, VoteType.UP));
    }

    @Test
    void togglingUpToDownAdjustsScoreAndReputationByExactDelta() {
        Answer answer = answerFromBob();
        service.vote(carol, answer, VoteType.UP);   // +10, score 1
        service.vote(carol, answer, VoteType.DOWN); // now -2, score -1

        assertEquals(-1, answer.getScore());
        // reputation went +10 then (-2 - +10) = -12 => net -2
        assertEquals(-2, bob.getReputation());
    }

    @Test
    void retractingVoteResetsScoreAndReputation() {
        Answer answer = answerFromBob();
        service.vote(carol, answer, VoteType.UP);
        service.retractVote(carol, answer);

        assertEquals(0, answer.getScore());
        assertEquals(0, bob.getReputation());
    }

    @Test
    void reVotingTheSameWayIsANoOp() {
        Answer answer = answerFromBob();
        service.vote(carol, answer, VoteType.UP);
        VoteChange second = service.vote(carol, answer, VoteType.UP);

        assertTrue(second.isNoOp());
        assertEquals(1, answer.getScore());  // not double counted
        assertEquals(10, bob.getReputation());
    }

    @Test
    void onlyQuestionAuthorCanAcceptAndItAwardsReputation() {
        Question q = service.postQuestion(alice, "t", "b", Set.of("java"));
        Answer answer = service.postAnswer(bob, q, "answer");

        assertThrows(UnauthorizedActionException.class, () -> service.acceptAnswer(carol, answer));

        service.acceptAnswer(alice, answer);
        assertTrue(answer.isAccepted());
        assertEquals(15, bob.getReputation());
    }

    @Test
    void switchingAcceptedAnswerMovesReputation() {
        Question q = service.postQuestion(alice, "t", "b", Set.of("java"));
        Answer first = service.postAnswer(bob, q, "first");
        User dave = service.registerUser("dave");
        Answer second = service.postAnswer(dave, q, "second");

        service.acceptAnswer(alice, first);   // bob +15
        service.acceptAnswer(alice, second);  // dave +15, bob -15

        assertFalse(first.isAccepted());
        assertTrue(second.isAccepted());
        assertEquals(0, bob.getReputation());
        assertEquals(15, dave.getReputation());
    }

    @Test
    void searchByTagKeywordAndAuthor() {
        Question java = service.postQuestion(alice, "Java threads", "about threads", Set.of("java"));
        Question python = service.postQuestion(bob, "Python GIL", "about the GIL", Set.of("python"));

        assertEquals(List.of(java), service.search(new TagSearchStrategy("java")));
        assertEquals(List.of(python), service.search(new KeywordSearchStrategy("gil")));
        assertEquals(List.of(java), service.search(new AuthorSearchStrategy(alice.getId())));
    }

    @Test
    void commentsAreAttachedToPosts() {
        Answer answer = answerFromBob();
        service.addComment(carol, answer, "nice answer");
        assertEquals(1, answer.getComments().size());
        assertEquals("nice answer", answer.getComments().get(0).text());
    }
}
