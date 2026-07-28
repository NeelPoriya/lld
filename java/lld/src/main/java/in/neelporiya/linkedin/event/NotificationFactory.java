package in.neelporiya.linkedin.event;

import in.neelporiya.linkedin.model.ConnectionRequest;
import in.neelporiya.linkedin.model.JobApplication;
import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Notification;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

/** // DESIGN PATTERN: Factory — centralizes notification text/id/time creation. */
public class NotificationFactory {

    private final Clock clock;
    private final Supplier<String> idGenerator;

    public NotificationFactory(Clock clock, Supplier<String> idGenerator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public Notification connectionRequest(ConnectionRequest request) {
        return new Notification(idGenerator.get(), request.getRecipient().getId(),
                request.getSender().getName() + " sent you a connection request", clock.instant());
    }

    public Notification endorsement(Member endorsed, Member endorser, String skill) {
        return new Notification(idGenerator.get(), endorsed.getId(),
                endorser.getName() + " endorsed your " + skill + " skill", clock.instant());
    }

    public Notification jobApplication(JobApplication application) {
        return new Notification(idGenerator.get(), application.job().getCompany().getId(),
                application.applicant().getName() + " applied to " + application.job().getTitle(), clock.instant());
    }
}
