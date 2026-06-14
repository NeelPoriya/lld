package in.neelporiya.phases.phase05polymorphism;

import in.neelporiya.phases.phase04inheritance.Dog;
import in.neelporiya.runner.Concept;

public class CovariantReturnDemo implements Concept {
    @Override
    public String title() {
        return "Covariant Return Demo";
    }

    @Override
    public String description() {
        return "an overriding method may return a subtype of what the parent method returns.";
    }

    @Override
    public void run() {
        // Covariant are return-only, meaning replacing only the return statement of base type to subtype
        // and if you change the parameters -> that's not covariant

        Dog d = new Dog("Tom");
        Dog child = d.reproduce();

        System.out.println(child.fetch());
    }
}
