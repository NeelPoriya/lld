package in.neelporiya.phases.phase04inheritance;

public class Animal {
    protected final String name;
    protected String tag = "animal";

    public Animal(String name) {
        this.name = name;
        System.out.println("Constructor for Animal called");
    }

    public String speak() {
        return name + " made a sound...";
    }

    // Used for covariant returns in Phase 05 - Polymorphism
    public Animal reproduce() {
        return new Animal("baby");
    }
}
