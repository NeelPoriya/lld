package in.neelporiya.phases.phase03encapsulation;

import java.util.List;

public class TeamMutable {
    private final List<String> members;
    private final String name;

    TeamMutable(String name, List<String> members) {
        this.name = name;
        this.members = members;
    }

    public List<String> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }
}
