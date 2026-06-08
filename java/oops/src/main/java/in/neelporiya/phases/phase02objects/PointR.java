package in.neelporiya.phases.phase02objects;

public record PointR(int x, int y) {
    public PointR {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
    }
}
