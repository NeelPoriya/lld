package in.neelporiya.parkinglot.vehicle;

/**
 * // DESIGN PATTERN: Factory Method.
 *
 * <p>Centralizes the {@code VehicleType -> concrete Vehicle} mapping so clients (gates, parsers,
 * REST controllers) never {@code new} a specific subclass. Adding a new type touches only this
 * switch, and the exhaustive switch means the compiler flags us if we forget a case.
 */
public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(VehicleType type, String licensePlate) {
        return switch (type) {
            case MOTORCYCLE -> new Motorcycle(licensePlate);
            case CAR -> new Car(licensePlate);
            case TRUCK -> new Truck(licensePlate);
        };
    }
}
