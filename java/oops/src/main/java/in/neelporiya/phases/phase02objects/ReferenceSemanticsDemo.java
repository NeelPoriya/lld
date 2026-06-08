package in.neelporiya.phases.phase02objects;

import in.neelporiya.runner.Concept;

import java.util.ArrayList;
import java.util.List;

public class ReferenceSemanticsDemo implements Concept {
    @Override
    public String title() {
        return "Pass by Value in Java";
    }

    @Override
    public String description() {
        return "Everything is pass by value in Java. For objects, the thing copied is the reference, not the object";
    }

    @Override
    public void run() {
        // 1. Primitive copy
        System.out.println("Scenario 1. Primitive copy");
        int n = 10;
        System.out.println("Before incrementing n = " + n);
        incrementN(n);
        System.out.println("After incrementing n = " + n + ". Value remains unchanged, because its pass by value");

        // 2. Object mutation invisible
        System.out.println("\nScenario 2. Object mutation visible");
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3));
        System.out.println("Before adding 4: a = " + a);
        addFour(a);
        System.out.println("After adding 4: a = " + a);

        // 3. Reassignment invisible
        System.out.println("\nScenario 3: Reassignment invisible");
        List<String> b = new ArrayList<>(List.of("John", "Jane", "Julie"));
        System.out.println("Before reassigning, b = " + b);
        reassignList(b);
        System.out.println("After reassigning, b = " + b);

        // 4. Swap fails
        System.out.println("\nScenario 4: Swap Fails");
        Point p1 = new Point(1, 2);
        Point p2 = new Point(3, 4);
        System.out.println("Before Swapping. (p1, p2): (" + p1 + ", " + p2 + ")");
        swap(p1, p2);
        System.out.println("After Swapping. (p1, p2): (" + p1 + ", " + p2 + ")");
    }

    private void swap(Point p1, Point p2) {
        Point t = p1;
        p1 = p2;
        p2 = t;
    }

    private void reassignList(List<String> b) {
        b = new ArrayList<>(List.of("Test1", "Test2"));
    }

    private void addFour(List<Integer> a) {
        a.add(4);
    }

    private void incrementN(int n) {
        n++;
    }
}
