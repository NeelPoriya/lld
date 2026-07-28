package in.neelporiya.linkedin.event;

import in.neelporiya.linkedin.model.ConnectionRequest;
import in.neelporiya.linkedin.model.JobApplication;
import in.neelporiya.linkedin.model.Member;

/** // DESIGN PATTERN: Observer — actions emit events; notification delivery is a listener. */
public interface LinkedInEventListener {
    default void onConnectionRequestSent(ConnectionRequest request) {
    }

    default void onSkillEndorsed(Member endorsed, Member endorser, String skill) {
    }

    default void onJobApplied(JobApplication application) {
    }
}
