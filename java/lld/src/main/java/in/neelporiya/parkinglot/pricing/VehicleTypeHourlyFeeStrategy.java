package in.neelporiya.parkinglot.pricing;

import in.neelporiya.parkinglot.ParkingTicket;
import in.neelporiya.parkinglot.vehicle.VehicleType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Charges a per-hour rate that depends on the vehicle type, rounding partial hours <em>up</em> and
 * charging a minimum of one hour.
 */
public class VehicleTypeHourlyFeeStrategy implements FeeStrategy {

    private final Map<VehicleType, BigDecimal> hourlyRates;
    private final BigDecimal fallbackRate;

    public VehicleTypeHourlyFeeStrategy(Map<VehicleType, BigDecimal> hourlyRates, BigDecimal fallbackRate) {
        // Defensive copy into an EnumMap (compact + fast for enum keys).
        this.hourlyRates = new EnumMap<>(hourlyRates);
        this.fallbackRate = fallbackRate;
    }

    /** Sensible defaults so callers/tests can construct one without a rate table. */
    public static VehicleTypeHourlyFeeStrategy withDefaults() {
        Map<VehicleType, BigDecimal> rates = new EnumMap<>(VehicleType.class);
        rates.put(VehicleType.MOTORCYCLE, new BigDecimal("10"));
        rates.put(VehicleType.CAR, new BigDecimal("20"));
        rates.put(VehicleType.TRUCK, new BigDecimal("40"));
        return new VehicleTypeHourlyFeeStrategy(rates, new BigDecimal("20"));
    }

    @Override
    public BigDecimal calculateFee(ParkingTicket ticket, Instant exitTime) {
        Duration parked = Duration.between(ticket.getEntryTime(), exitTime);
        long billableHours = ceilHours(parked);
        BigDecimal rate = hourlyRates.getOrDefault(ticket.getVehicle().getType(), fallbackRate);
        return rate.multiply(BigDecimal.valueOf(billableHours));
    }

    /** Round the duration up to whole hours, with a floor of 1 hour (so a 2-minute stay still pays). */
    private static long ceilHours(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long hours = (seconds + 3599) / 3600; // ceil division
        return Math.max(1, hours);
    }
}
