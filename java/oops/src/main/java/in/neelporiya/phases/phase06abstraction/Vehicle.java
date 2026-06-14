package in.neelporiya.phases.phase06abstraction;

public abstract class Vehicle {
    protected String name;
    protected Vehicle(String name) { this.name = name; }

    public abstract void start();
    public String describe() {
        return name + " is a vehicle";
    }
}
