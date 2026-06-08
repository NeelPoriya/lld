package in.neelporiya.phases.phase02objects;

import in.neelporiya.runner.Concept;

import java.util.HashSet;
import java.util.Set;

public class ClassBasicsDemo implements Concept {
    @Override
    public String title() {
        return "Introduction to classes in Java";
    }

    @Override
    public String description() {
        return "Creating a basic class with some private and static fields, additionally we understand " +
                "the meaning of hashCode(), equals(Object o) and toString() methods in a class";
    }

    @Override
    public void run() {
        Point p1 = new Point(5, 10);
        Point p2 = new Point(10, 20);
        Point p3 = new Point();

        Set<Point> set = new HashSet<>();
        set.add(p3);

        System.out.println(Point.getInstanceCount());
        System.out.println(p1);
        System.out.println(p2 == p1);
        System.out.println(p2.equals(p1));
        System.out.println(set.contains(new Point()));
    }
}
