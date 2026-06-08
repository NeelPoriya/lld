package in.neelporiya.phases.phase04inheritance;

public class Dog extends Animal{
    protected String tag = "dog";

    public Dog(String name) {
        super(name);
        System.out.println("Constructor for Dog called");
    }

    @Override
    public String speak() {
        return name + " barks...";
    }

    public String fetch() {
        return super.speak() + ", then fetches";
    }
}
