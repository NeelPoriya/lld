package in.neelporiya.phases.phase03encapsulation;

import in.neelporiya.runner.Concept;

import java.util.ArrayList;
import java.util.List;

public class ImmutabilityDemo implements Concept {
    @Override
    public String title() {
        return "Ensuring Immutability in Java";
    }

    @Override
    public String description() {
        return "How passing values can actually lead to some " +
                "very drastic unwanted behaviors";
    }

    @Override
    public void run() {
        // Mutable example
        System.out.println("Mutable Object");
        List<String> members = new ArrayList<>(List.of("John", "Sarah"));
        TeamMutable tm = new TeamMutable("Team 1", members);

        System.out.println("Team members before modification: " + tm.getMembers());
        members.add("Jane");
        tm.getMembers().add("Larry");
        System.out.println("Team members after modification: " + tm.getMembers());

        members = new ArrayList<>(List.of("John", "Sarah"));

        System.out.println("\nImmutable Object");
        TeamImmutable tim = new TeamImmutable("Team 2", members);

        System.out.println("Team members before modification: " + tim.getMembers());
        members.add("Jane");
        try {
            tim.getMembers().add("Larry");
        } catch (UnsupportedOperationException e) {
            System.out.println("Operation is not allowed.");
        }
        System.out.println("Team members after modification: " + tim.getMembers());
    }
}
