package in.neelporiya.phases.phase06abstraction;

import java.util.Comparator;
import java.util.Objects;

public class Person implements Comparable<Person> {
    private final String name;
    private final int age;

    private static final Comparator<Person> COMPARATOR = Comparator.
            comparingInt((Person p) -> p.age)
            .thenComparing(p -> p.name);

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public String toString() {
        return "Person[name=" + name + ", age=" + age + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Person p)) return false;
        return p.name.equals(name) && p.age == age;
    }

    @Override
    public int compareTo(Person p) {
        return COMPARATOR.compare(this, p);
    }
}
