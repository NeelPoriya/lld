package in.neelporiya.phases.phase05polymorphism;

import in.neelporiya.phases.phase03encapsulation.Circle;
import in.neelporiya.phases.phase03encapsulation.Rectangle;
import in.neelporiya.phases.phase03encapsulation.Shape;
import in.neelporiya.phases.phase03encapsulation.Triangle;
import in.neelporiya.phases.phase04inheritance.Animal;
import in.neelporiya.phases.phase04inheritance.Dog;
import in.neelporiya.runner.Concept;

public class SwitchPatternMatchingDemo implements Concept {
    @Override
    public String title() {
        return "Switch pattern matching in Java";
    }

    @Override
    public String description() {
        return "switch statements are full type-dispatcher with destructuring";
    }

    @Override
    public void run() {
        Animal animal = new Dog("Tom");
        System.out.println(describe(animal));

        Cat c = new Cat("Catherine");
        System.out.println(describe(c));

        Rectangle r = new Rectangle(10, 20);
        System.out.println(area(r));
        System.out.println(classify(r));
    }

    String describe(Animal a) {
        return switch (a) {
            case Dog d when d.fetch().startsWith("T") -> "this is a guarded check";
            case Dog d -> d.fetch();
            case Cat c -> c.speak();
            case null -> "woah! animal is null";
            default -> "unknown animal";
        };
    }

    double area(Shape shape) {
        return switch(shape) {
            case Rectangle r -> r.width() * r.height();
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Triangle t -> 0.5 * t.base() * t.height();
        };
    }

    String classify(Shape shape) {
        return switch (shape) {
            case Rectangle(double w, double h) when w == h -> "This is a square";
            case Rectangle(double w, double h) -> "This is a normal rectangle with base " + w + " and height " + h;
            case Triangle(double b, double h) -> "Triangle b = " + b + " and h = " + h;
            case Circle(double rad) when rad > 100 -> "Big circle";
            case Circle(double rad) -> "circle with radius " + rad;
        };
    }
}
