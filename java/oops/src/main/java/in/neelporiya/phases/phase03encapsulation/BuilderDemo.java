package in.neelporiya.phases.phase03encapsulation;

import in.neelporiya.runner.Concept;

public class BuilderDemo implements Concept {
    @Override
    public String title() {
        return "Builder pattern in Java";
    }

    @Override
    public String description() {
        return "Builder is the most used pattern in Java because it doesn't have " +
                "named arguments, optional parameters and object initializers";
    }

    @Override
    public void run() {
        User user = new User.Builder("John")
                .age(21)
                .email("john@example.com")
                .build();

        System.out.println(user);
    }
}
