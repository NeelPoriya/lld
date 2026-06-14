package in.neelporiya.phases.phase05polymorphism;

import in.neelporiya.phases.phase04inheritance.Animal;
import in.neelporiya.phases.phase04inheritance.Dog;
import in.neelporiya.runner.Concept;

public class CastingDemo implements Concept {
    @Override
    public String title() {
        return "Polymorphism in Java";
    }

    @Override
    public String description() {
        return "Polymorphism consists of class methods being virtual-by-default (which was covered in phase 04), " +
                "overload resolution (widening-beats-boxing), casting, instanceof and pattern matching";
    }

    @Override
    public void run() {
        Animal animalCat = new Cat("Jennifer");
        Animal animalDog = new Dog("Robert");

        Cat cat = (Cat) animalCat;
        System.out.println(cat.speak());

        if (animalDog instanceof Dog d) {
            System.out.println(d.fetch());
        }

        try {
            Cat c = (Cat) animalDog;
        } catch (ClassCastException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }

        Animal n = null;
        System.out.println(n instanceof Dog d);
    }
}
