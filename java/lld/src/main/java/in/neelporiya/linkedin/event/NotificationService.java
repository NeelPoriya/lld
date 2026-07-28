package in.neelporiya.linkedin.event;

import in.neelporiya.linkedin.model.ConnectionRequest;
import in.neelporiya.linkedin.model.Company;
import in.neelporiya.linkedin.model.JobApplication;
import in.neelporiya.linkedin.model.Member;
import in.neelporiya.linkedin.model.Notification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService implements LinkedInEventListener {

    private final NotificationFactory factory;
    private final Map<String, List<Notification>> notificationsByRecipientId = new ConcurrentHashMap<>();

    public NotificationService(NotificationFactory factory) {
        this.factory = factory;
    }

    @Override
    public void onConnectionRequestSent(ConnectionRequest request) {
        store(factory.connectionRequest(request));
    }

    @Override
    public void onSkillEndorsed(Member endorsed, Member endorser, String skill) {
        store(factory.endorsement(endorsed, endorser, skill));
    }

    @Override
    public void onJobApplied(JobApplication application) {
        store(factory.jobApplication(application));
    }

    private void store(Notification notification) {
        notificationsByRecipientId.computeIfAbsent(notification.recipientId(), ignored -> new CopyOnWriteArrayList<>())
                .add(notification);
    }

    public List<Notification> notificationsFor(Member member) {
        return notificationsForRecipientId(member.getId());
    }

    public List<Notification> notificationsFor(Company company) {
        return notificationsForRecipientId(company.getId());
    }

    private List<Notification> notificationsForRecipientId(String recipientId) {
        return List.copyOf(notificationsByRecipientId.getOrDefault(recipientId, List.of()));
    }
}
