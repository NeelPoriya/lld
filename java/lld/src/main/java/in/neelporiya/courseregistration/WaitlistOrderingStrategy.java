package in.neelporiya.courseregistration;

import java.util.Deque;

/**
 * // DESIGN PATTERN: Strategy. FIFO is the default, but interviews can easily extend this with
 * seniority, graduating-year priority or accessibility rules without changing RegistrationService.
 */
public interface WaitlistOrderingStrategy {

    String selectNextStudent(Deque<String> waitlistedStudentIds);
}
