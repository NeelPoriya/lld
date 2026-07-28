package in.neelporiya.snakeandladder;

/**
 * A board transition triggered after landing on a cell.
 */
public interface Jump {

    int from();

    int to();

    default int apply() {
        return to();
    }
}
