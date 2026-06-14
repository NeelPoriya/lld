package in.neelporiya.phases.phase05polymorphism;

import in.neelporiya.phases.phase04inheritance.Animal;

public class Cat extends Animal {
    public Cat(String name) {
        super(name);
    }

    @Override
    public String speak() {
        return this.name + " meows...";
    }
}
