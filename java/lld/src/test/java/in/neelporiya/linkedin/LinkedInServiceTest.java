package in.neelporiya.linkedin;

import in.neelporiya.linkedin.event.LinkedInEventListener;
import in.neelporiya.linkedin.model.Company;
import in.neelporiya.linkedin.model.ConnectionRequest;
import in.neelporiya.linkedin.model.ConnectionRequestStatus;
import in.neelporiya.linkedin.model.Job;
import in.neelporiya.linkedin.model.JobApplication;
import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Post;
import in.neelporiya.linkedin.model.Profile;
import in.neelporiya.linkedin.service.LinkedInService;
import in.neelporiya.testutil.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedInServiceTest {

    private MutableClock clock;
    private LinkedInService service;
    private Member alice;
    private Member bob;
    private Member carol;

    @BeforeEach
    void setUp() {
        AtomicInteger seq = new AtomicInteger();
        clock = MutableClock.atEpoch();
        service = new LinkedInService(clock, () -> "id-" + seq.incrementAndGet());
        alice = service.registerMember("Alice", profile("Staff Engineer", "java", "leadership"));
        bob = service.registerMember("Bob", profile("Backend Engineer", "java", "distributed systems"));
        carol = service.registerMember("Carol", profile("Recruiter", "hiring"));
    }

    @Test
    void acceptingConnectionRequestCreatesMutualFirstDegreeConnection() {
        ConnectionRequest request = service.sendConnectionRequest(alice, bob);
        service.acceptConnectionRequest(request);

        assertEquals(ConnectionRequestStatus.ACCEPTED, request.getStatus());
        assertTrue(alice.isConnectedTo(bob));
        assertTrue(bob.isConnectedTo(alice));
        assertEquals(1, service.connectionDegree(alice, bob));
    }

    @Test
    void secondDegreeConnectionIsFriendOfFriend() {
        service.acceptConnectionRequest(service.sendConnectionRequest(alice, bob));
        service.acceptConnectionRequest(service.sendConnectionRequest(bob, carol));

        assertEquals(2, service.connectionDegree(alice, carol));
        assertEquals(2, service.connectionDegree(carol, alice));
    }

    @Test
    void rejectingConnectionRequestDoesNotConnectMembers() {
        ConnectionRequest request = service.sendConnectionRequest(alice, bob);
        service.rejectConnectionRequest(request);

        assertEquals(ConnectionRequestStatus.REJECTED, request.getStatus());
        assertFalse(alice.isConnectedTo(bob));
        assertFalse(bob.isConnectedTo(alice));
        assertThrows(IllegalStateException.class, () -> service.acceptConnectionRequest(request));
    }

    @Test
    void endorsingSkillIsIdempotentPerEndorserMemberAndSkill() {
        assertTrue(service.endorseSkill(bob, alice, "java"));
        assertFalse(service.endorseSkill(bob, alice, "java"));

        assertEquals(1, alice.endorsementCount("java"));
        assertEquals(1, service.notificationsFor(alice).size());
    }

    @Test
    void feedContainsConnectionPostsNewestFirstUsingInjectedClock() {
        service.acceptConnectionRequest(service.sendConnectionRequest(alice, bob));

        Post oldConnectionPost = service.createPost(alice, "old connection update");
        clock.advance(Duration.ofMinutes(1));
        Post strangerPost = service.createPost(carol, "not connected update");
        clock.advance(Duration.ofMinutes(1));
        Post ownPost = service.createPost(bob, "own update");
        clock.advance(Duration.ofMinutes(1));
        Post newConnectionPost = service.createPost(alice, "new connection update");

        assertEquals(List.of(newConnectionPost, oldConnectionPost), service.newsFeedFor(bob));
        assertFalse(service.newsFeedFor(bob).contains(strangerPost));
        assertFalse(service.newsFeedFor(bob).contains(ownPost));
    }

    @Test
    void companyPostsJobAndMemberAppliesWithNotification() {
        List<String> events = new ArrayList<>();
        service.addListener(new LinkedInEventListener() {
            @Override
            public void onJobApplied(JobApplication application) {
                events.add(application.applicant().getName() + ":" + application.job().getTitle());
            }
        });
        Company company = service.registerCompany("Acme");
        Job job = service.postJob(company, "Java Developer", "Build backend systems");

        JobApplication application = service.applyToJob(alice, job);

        assertEquals(alice, application.applicant());
        assertTrue(job.hasApplicant(alice));
        assertEquals(1, job.applicationCount());
        assertEquals(List.of("Alice:Java Developer"), events);
        assertEquals(1, service.notificationsFor(company).size());
    }

    @Test
    void searchesMembersByNameAndSkillAndJobsByKeyword() {
        Company company = service.registerCompany("Acme");
        Job javaJob = service.postJob(company, "Java Developer", "Build backend systems");
        service.postJob(company, "Product Manager", "Roadmap and discovery");

        assertEquals(List.of(alice), service.searchMembersByName("lic"));
        assertEquals(List.of(alice, bob), service.searchMembersBySkill("java"));
        assertEquals(List.of(javaJob), service.searchJobs("backend"));
    }

    private static Profile profile(String headline, String... skills) {
        Profile.Builder builder = Profile.builder().headline(headline);
        for (String skill : skills) {
            builder.addSkill(skill);
        }
        return builder.build();
    }
}
