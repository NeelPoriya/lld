package in.neelporiya.phases.phase03encapsulation;

import in.neelporiya.runner.Concept;

public class NestedClassDemo implements Concept {
    @Override
    public String title() {
        return "Nested classes in Java";
    }

    @Override
    public String description() {
        return "There are four kinds of nested classes: 1. Static, 2. Inner (non-static), " +
                "3. Local (inside a helper method), 4. Anonymous";
    }

    @Override
    public void run() {
        // 1. Static class. Builder is a static nested class.
        User user = new User.Builder("John")
                .email("john@example.com")
                .age(21)
                .build();

        System.out.println(user);

        // 2. Inner (non-static)
        Car c = new Car();
        Car.Dashboard d = c.new Dashboard();
        d.show();

        // 3. Local (inside a helper method)
        help();

        // 4. Anonymous
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("This is being called from an anonymous class's method");
            }
        };
        r.run();
    }

    private void help() {
        class Test { void show() {
                System.out.println("This is show inside Test class");
            }
        }
        new Test().show();
    }
}
