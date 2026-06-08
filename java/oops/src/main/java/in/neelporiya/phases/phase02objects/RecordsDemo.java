package in.neelporiya.phases.phase02objects;

import in.neelporiya.runner.Concept;

public class RecordsDemo implements Concept {
    @Override
    public String title() {
        return "Records in Java";
    }

    @Override
    public String description() {
        return "Showing how powerful records are which takes care of lot of things internally " +
                "which we need to consider while classes with custom methods " +
                "for toString(), equals(Object o) and hashCode()";
    }

    @Override
    public void run() throws Exception {
        PointR p1 = new PointR(5, 10);
        PointR p2 = new PointR(5, 10);

        try {
            PointR p3 = new PointR(-3, -5);
        } catch (IllegalArgumentException ex) {
            System.out.println("Caught: " + ex.getMessage());
        }

        System.out.println(p1);
        System.out.println(p1.x());
        System.out.println(p1.equals(p2));
        System.out.println(p1 == p2);
    }
}
