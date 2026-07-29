package in.neelporiya.courseregistration;

import java.util.Deque;

public class FifoWaitlistOrderingStrategy implements WaitlistOrderingStrategy {

    @Override
    public String selectNextStudent(Deque<String> waitlistedStudentIds) {
        return waitlistedStudentIds.pollFirst();
    }
}
