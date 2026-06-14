package in.neelporiya.phases.phase07composition;

import in.neelporiya.runner.Concept;

import java.util.List;

public class CompositionDemo implements Concept {
    @Override
    public String title() {
        return "Composition in Java";
    }

    @Override
    public String description() {
        return "We always prefer composition over inheritance. Inheritance (is-a) relationship is not " +
                "best suited for most of the scenarios. When two models have (has-a) or (uses-a) relation" +
                " between them, composition is usually better.";
    }

    // composition strong (has-a) relation
    public static class Engine {
        public void start() {
            System.out.println("Starting engine");
        }
    }

    public static class Car {
        private final Engine engine = new Engine();
        public void start() {
            engine.start();
        }
    }

    // aggreation - weak (has-a) relation
    public static class Member {
        private final String name;
        public Member(String name) {
            this.name = name;
        }
        public String name() {
            return this.name;
        }

        @Override
        public String toString() {
            return "Member[name=" + name + "]";
        }
    }

    public static class Team {
        private final List<Member> players;
        public Team(List<Member> players) {
            this.players = players;
        }
        public List<Member> getMembers() {
            return players;
        }
    }

    // Association
    public static class Driver {
        void drive(Car c) {
            c.start();
        }
    }

    @Override
    public void run() {
        Car car = new Car();
        car.start();

        Member m1 = new Member("Player 1");
        Member m2 = new Member("Player 2");
        Team team = new Team(List.of(m1, m2));

        System.out.println(team.getMembers());

        Driver d = new Driver();
        d.drive(car);
    }
}
