package in.neelporiya.linkedin.service;

import in.neelporiya.linkedin.event.LinkedInEventListener;
import in.neelporiya.linkedin.event.NotificationFactory;
import in.neelporiya.linkedin.event.NotificationService;
import in.neelporiya.linkedin.feed.ChronologicalFeedStrategy;
import in.neelporiya.linkedin.feed.NewsFeedStrategy;
import in.neelporiya.linkedin.model.Company;
import in.neelporiya.linkedin.model.ConnectionRequest;
import in.neelporiya.linkedin.model.Job;
import in.neelporiya.linkedin.model.JobApplication;
import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Notification;
import in.neelporiya.linkedin.model.Post;
import in.neelporiya.linkedin.model.Profile;
import in.neelporiya.linkedin.repository.CompanyRepository;
import in.neelporiya.linkedin.repository.JobRepository;
import in.neelporiya.linkedin.repository.MemberRepository;
import in.neelporiya.linkedin.repository.PostRepository;
import in.neelporiya.linkedin.search.JobSearchStrategy;
import in.neelporiya.linkedin.search.KeywordJobSearchStrategy;
import in.neelporiya.linkedin.search.MemberSearchStrategy;
import in.neelporiya.linkedin.search.NameMemberSearchStrategy;
import in.neelporiya.linkedin.search.SkillMemberSearchStrategy;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * // DESIGN PATTERN: Facade — clients use one API over profiles, connections, endorsements, posts,
 * jobs, search strategies, repositories, and observer notifications.
 *
 * <p>// TESTABILITY: {@link Clock} and id {@link Supplier} are injected. Tests use MutableClock to
 * advance feed timestamps deterministically and deterministic suppliers for stable ids.
 */
public class LinkedInService {

    private final MemberRepository memberRepository = new MemberRepository();
    private final CompanyRepository companyRepository = new CompanyRepository();
    private final JobRepository jobRepository = new JobRepository();
    private final PostRepository postRepository = new PostRepository();
    private final List<LinkedInEventListener> listeners = new CopyOnWriteArrayList<>();

    private final Clock clock;
    private final Supplier<String> idGenerator;
    private final NewsFeedStrategy feedStrategy;
    private final NotificationService notificationService;

    public LinkedInService(Clock clock, Supplier<String> idGenerator) {
        this(clock, idGenerator, new ChronologicalFeedStrategy());
    }

    public LinkedInService(Clock clock, Supplier<String> idGenerator, NewsFeedStrategy feedStrategy) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.feedStrategy = Objects.requireNonNull(feedStrategy, "feedStrategy");
        this.notificationService = new NotificationService(new NotificationFactory(clock, idGenerator));
        listeners.add(notificationService);
    }

    public static LinkedInService createDefault() {
        return new LinkedInService(Clock.systemUTC(), () -> UUID.randomUUID().toString());
    }

    public void addListener(LinkedInEventListener listener) {
        listeners.add(listener);
    }

    public Member registerMember(String name, Profile profile) {
        Member member = new Member(idGenerator.get(), name, profile);
        memberRepository.save(member);
        return member;
    }

    public Company registerCompany(String name) {
        Company company = new Company(idGenerator.get(), name);
        companyRepository.save(company);
        return company;
    }

    public ConnectionRequest sendConnectionRequest(Member sender, Member recipient) {
        if (sender.equals(recipient)) {
            throw new IllegalArgumentException("Cannot connect to yourself");
        }
        if (sender.isConnectedTo(recipient)) {
            throw new IllegalStateException("Members are already connected");
        }
        ConnectionRequest request = new ConnectionRequest(idGenerator.get(), sender, recipient, clock.instant());
        listeners.forEach(listener -> listener.onConnectionRequestSent(request));
        return request;
    }

    /**
     * // CONCURRENCY: accept/reject race on the same request is guarded by synchronizing on the
     * request. Both members are then locked in deterministic id order before writing both connection
     * sets, preserving the bidirectional invariant and avoiding deadlock.
     */
    public void acceptConnectionRequest(ConnectionRequest request) {
        synchronized (request) {
            if (!request.isPending()) {
                throw new IllegalStateException("Connection request is no longer pending");
            }
            Member first = request.getSender().getId().compareTo(request.getRecipient().getId()) <= 0
                    ? request.getSender()
                    : request.getRecipient();
            Member second = first.equals(request.getSender()) ? request.getRecipient() : request.getSender();
            synchronized (first) {
                synchronized (second) {
                    request.getSender().addConnection(request.getRecipient());
                    request.getRecipient().addConnection(request.getSender());
                    request.markAccepted();
                }
            }
        }
    }

    public void rejectConnectionRequest(ConnectionRequest request) {
        synchronized (request) {
            request.markRejected();
        }
    }

    public int connectionDegree(Member from, Member to) {
        if (from.equals(to)) {
            return 0;
        }
        if (from.isConnectedTo(to) && to.isConnectedTo(from)) {
            return 1;
        }
        for (String connectionId : from.getConnectionIds()) {
            Member connection = memberRepository.findById(connectionId);
            if (connection != null && connection.isConnectedTo(to)) {
                return 2;
            }
        }
        return -1;
    }

    /** // INTERVIEW INSIGHT: notify only when the endorsement set actually changed. */
    public boolean endorseSkill(Member endorser, Member endorsed, String skill) {
        boolean changed = endorsed.endorseSkill(endorser, skill);
        if (changed) {
            listeners.forEach(listener -> listener.onSkillEndorsed(endorsed, endorser, skill));
        }
        return changed;
    }

    public Post createPost(Member author, String text) {
        Post post = new Post(idGenerator.get(), author, text, clock.instant());
        postRepository.save(post);
        return post;
    }

    public List<Post> newsFeedFor(Member viewer) {
        Set<String> authorIds = new HashSet<>(viewer.getConnectionIds());
        return feedStrategy.rank(viewer, postRepository.findByAuthorIds(authorIds));
    }

    public Job postJob(Company company, String title, String description) {
        Job job = new Job(idGenerator.get(), company, title, description, clock.instant());
        company.addJob(job);
        jobRepository.save(job);
        return job;
    }

    /**
     * // CONCURRENCY: the Job owns a concurrent applicant set. Only the first apply for a member emits
     * an application object and notification, so retries are safe.
     */
    public JobApplication applyToJob(Member applicant, Job job) {
        if (!job.apply(applicant)) {
            throw new IllegalStateException("Member already applied to this job");
        }
        JobApplication application = new JobApplication(idGenerator.get(), applicant, job, clock.instant());
        listeners.forEach(listener -> listener.onJobApplied(application));
        return application;
    }

    public List<Member> searchMembersByName(String query) {
        return searchMembers(query, new NameMemberSearchStrategy());
    }

    public List<Member> searchMembersBySkill(String query) {
        return searchMembers(query, new SkillMemberSearchStrategy());
    }

    public List<Member> searchMembers(String query, MemberSearchStrategy strategy) {
        return strategy.search(memberRepository.findAll(), query);
    }

    public List<Job> searchJobs(String query) {
        return searchJobs(query, new KeywordJobSearchStrategy());
    }

    public List<Job> searchJobs(String query, JobSearchStrategy strategy) {
        return strategy.search(jobRepository.findAll(), query);
    }

    public List<Notification> notificationsFor(Member member) {
        return notificationService.notificationsFor(member);
    }

    public List<Notification> notificationsFor(Company company) {
        return notificationService.notificationsFor(company);
    }

    /** // EXTENSIBILITY: demo/test lookups expose repository results without exposing storage maps. */
    public Member findMember(String id) {
        return memberRepository.findById(id);
    }

    public Company findCompany(String id) {
        return companyRepository.findById(id);
    }

    public Job findJob(String id) {
        return jobRepository.findById(id);
    }

    public Post findPost(String id) {
        return postRepository.findById(id);
    }
}
