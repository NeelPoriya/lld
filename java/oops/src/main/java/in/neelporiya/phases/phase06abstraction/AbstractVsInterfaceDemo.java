package in.neelporiya.phases.phase06abstraction;

import in.neelporiya.runner.Concept;

public class AbstractVsInterfaceDemo implements Concept {
    @Override
    public String title() {
        return "Abstract class vs Interface in Java";
    }

    @Override
    public String description() {
        return "Abstract class useful when you want to store partial state in a class, " +
                "while interface are pure contracts. Only one class can be extended, " +
                "while multiple interface can be implemented";
    }

    @Override
    public void run() {
        Car c = new Car("Honda");
        c.start();
        c.drive();
        c.upgrade();

        // Vehicle v = new Vehicle("Hyundai"); <- Wrong, cannot instantiate abstract class.
    }
}
