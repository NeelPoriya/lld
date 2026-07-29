package in.neelporiya.ridesharing;

/**
 * // DESIGN PATTERN: Observer. Notifications, analytics, and audits subscribe to ride changes
 * without coupling those concerns to matching or pricing.
 */
public interface RideStatusObserver {

    void onRideStatusChanged(RideStatusEvent event);
}
