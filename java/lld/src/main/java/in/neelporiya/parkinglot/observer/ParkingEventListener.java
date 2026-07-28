package in.neelporiya.parkinglot.observer;

import in.neelporiya.parkinglot.ParkingReceipt;
import in.neelporiya.parkinglot.ParkingTicket;

/**
 * // DESIGN PATTERN: Observer.
 *
 * <p>Lets display boards, analytics, or "LOT FULL" signage react to parking events without the
 * {@code ParkingLot} knowing who is listening. Methods are {@code default} no-ops so a listener
 * overrides only the events it cares about (Interface Segregation in spirit).
 */
public interface ParkingEventListener {

    default void onVehicleParked(ParkingTicket ticket) {
    }

    default void onVehicleUnparked(ParkingReceipt receipt) {
    }
}
