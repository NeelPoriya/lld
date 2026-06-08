package in.neelporiya.phases.phase02objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Point {
    public int x;
    private int y;
    private static int instanceCount = 0;

    List<Integer> a;

    public List<Integer> a() {
        return a;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        instanceCount++;
        a = new ArrayList<>();
    }

    // If you write ZERO constructors, Java gives you a free no-args one
    // The moment you write ANY constructor, the free one DISAPPEARS
    public Point() {
        this(0, 0);
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point p)) return false;
        return p.x == x && p.y == y;
    }
}
