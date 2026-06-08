package in.neelporiya.phases.phase01basics;

import in.neelporiya.runner.Concept;

public class WrapperBoxingDemo implements Concept {
    @Override
    public String title() {
        return "Wrapping Boxing Demo";
    }

    @Override
    public String description() {
        return "Every primitive data type has an object wrapper. List<int> is illegal - must be List<Integer>";
    }

    @Override
    public void run() {
        Integer a = 1000;
        Integer b = 1000;
        System.out.println("Integer a = 1000; Integer b = 1000;");
        System.out.println("a == b: " + (a == b));
        System.out.println("a.equals(b) : " + a.equals(b));

        Integer x = 100;
        Integer y = 100;
        System.out.println("Cached variables, (Java caches boxed integer values from -128 to 127, " +
                "so == works for small numbers but breaks for larger ones: e.g., Integer x = 100, y = 100; x == y: " + (x == y));

        Integer p = 200;
        Integer q = 200;
        System.out.println("x == y (100 cached): " + (x == y));
        System.out.println("p == q (200, NOT cached): " + (p == q));

        try {
            Integer maybe = null;
            int oops = maybe; // NullPointerException at runtime - unboxing null
        } catch (Exception e) {
            System.out.println("Unboxing null threw: " + e.getMessage());
        }

        System.out.println("Integer parsing");
        System.out.println("int n = Integer.parseInt(\"42\"); // String -> primitive int");
        System.out.println("Integer m = Integer.valueOf(\"42\"); // String -> Integer (uses cache)");
    }
}
