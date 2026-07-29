package in.neelporiya.jobscheduler;

/**
 * // DESIGN PATTERN: Observer — react to job lifecycle (metrics, alerting, dead-letter handling)
 * without the scheduler depending on any of it.
 */
public interface JobExecutionListener {

    default void onStart(Job job) {
    }

    default void onSuccess(Job job) {
    }

    default void onRetry(Job job, Exception error) {
    }

    default void onFailure(Job job, Exception error) {
    }

    default void onCancel(Job job) {
    }
}
