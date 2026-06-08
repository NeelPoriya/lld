package in.neelporiya.phases.phase04inheritance;

import in.neelporiya.runner.Concept;

public class InheritanceDemo implements Concept {
    @Override
    public String title() {
        return "Inheritance in Java";
    }

    @Override
    public String description() {
        return "inheritance lets you extend functionality of a parent class into a child class";
    }

    @Override
    public void run() {
        Animal animal = new Dog("Tom");
        System.out.println(animal.speak());

        // the tag reads the value set in Animal, and not in Dog, because field values are determined
        // on compile-time where methods are resolved during runtime.
        System.out.println(animal.tag);
    }
}
