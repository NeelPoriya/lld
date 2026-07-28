package in.neelporiya.airline.model;

public final class Flight {

    private final String flightNumber;
    private final String origin;
    private final String destination;

    public Flight(String flightNumber, String origin, String destination) {
        this.flightNumber = Passenger.requireText(flightNumber, "flightNumber");
        this.origin = Passenger.requireText(origin, "origin");
        this.destination = Passenger.requireText(destination, "destination");
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}
