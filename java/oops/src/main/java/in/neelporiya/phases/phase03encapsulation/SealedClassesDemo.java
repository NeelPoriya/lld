package in.neelporiya.phases.phase03encapsulation;

import in.neelporiya.runner.Concept;

public class SealedClassesDemo implements Concept {
    @Override
    public String title() {
        return "Sealed classes in Java";
    }

    @Override
    public String description() {
        return "Sealed class in Java allows only certain classes to extend, implement existing classes/interface";
    }

    @Override
    public void run() {
        Triangle t = new Triangle(15, 20);
        Circle c = new Circle(20);
        Rectangle r = new Rectangle(10, 20);

        System.out.println(area(t));
        System.out.println(area(c));
        System.out.println(area(r));

        // Invalid class
        // public record Square(int length) implements Shape { }

        // Difference between sealed class in Java vs sealed class in C#
        // In Java, sealed class means, only the permitted classes can implement/extend given sealed class
        // While in C#, sealed class means, it cannot be inherited any further.
    }

    private double area(Shape s) {
        return switch (s) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> 0.5 * t.base() * t.height();
        };
    }
}
