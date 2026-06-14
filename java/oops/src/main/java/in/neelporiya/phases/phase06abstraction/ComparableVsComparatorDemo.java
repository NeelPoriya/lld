package in.neelporiya.phases.phase06abstraction;

import in.neelporiya.runner.Concept;

import java.util.*;

public class ComparableVsComparatorDemo implements Concept {
    @Override
    public String title() {
        return "Comparable and Comparator in Java";
    }

    @Override
    public String description() {
        return "Comparable is an interface which a class can implement and override the default " +
                "sorting for itself, while Comparator let's you define multiple sort orders and sort " +
                "a collection based off of any of them";
    }

    @Override
    public void run() {
        Person john = new Person("John", 20);
        Person jane = new Person("Jane", 24);
        Person oldJane = new Person("Jane", 50);
        Person sarah = new Person("Sarah", 30);
        Person bob = new Person("Bob", 24);

        System.out.println("Sort by age:");
        TreeSet<Person> treeSet = new TreeSet<>(List.of(john, jane, sarah, bob));
        System.out.println(treeSet);

        System.out.println("Sort by name:");
        Comparator<Person> byName = Comparator.comparing(Person::getName);
        List<Person> list = new ArrayList<>(List.of(john, sarah, jane));
        list.sort(byName);
        System.out.println(list);

        System.out.println("Multiple sort orders:");
        Comparator<Person> byNameAndReverseAge = Comparator.comparing(Person::getName)
                .thenComparing(Comparator.comparing(Person::getAge).reversed());
        list = new ArrayList<>(List.of(john, jane, oldJane, sarah));
        Collections.sort(list, byNameAndReverseAge);

        System.out.println(list);
    }
}
