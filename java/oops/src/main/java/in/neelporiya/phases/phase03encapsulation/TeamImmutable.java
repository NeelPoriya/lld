package in.neelporiya.phases.phase03encapsulation;

import java.util.ArrayList;
import java.util.List;

public class TeamImmutable {
    private final List<String> members;
    private final String name;

    TeamImmutable(String name, List<String> members) {
        this.name = name;
        this.members = new ArrayList<>(members);
    }

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        return List.copyOf(members);
    }
}
