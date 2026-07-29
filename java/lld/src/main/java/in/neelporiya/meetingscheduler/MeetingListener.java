package in.neelporiya.meetingscheduler;

/**
 * // DESIGN PATTERN: Observer — attendees/rooms get invite and cancellation notifications without the
 * scheduler depending on any notification channel.
 */
public interface MeetingListener {

    default void onScheduled(Meeting meeting) {
    }

    default void onCancelled(Meeting meeting) {
    }
}
