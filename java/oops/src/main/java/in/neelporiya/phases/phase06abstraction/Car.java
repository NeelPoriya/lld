package in.neelporiya.phases.phase06abstraction;

public class Car extends Vehicle implements Drivable, Upgradable{
    protected Car(String name) {
        super(name);
    }

    @Override
    public void drive() {
        System.out.println("Driving " + name + " vroom...");
    }

    @Override
    public void upgrade() {
        System.out.println("Upgrading " + name + ".");
    }

    @Override
    public void start() {
        System.out.println("Starting " + name + ".");
    }
}
