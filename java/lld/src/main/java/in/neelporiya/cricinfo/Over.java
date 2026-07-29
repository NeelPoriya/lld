package in.neelporiya.cricinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A six-legal-ball over, plus any wides/no-balls bowled inside it. */
public class Over {

    private final int number;
    private final Player bowler;
    private final List<Delivery> deliveries = new ArrayList<>();

    public Over(int number, Player bowler) {
        if (number < 1) {
            throw new IllegalArgumentException("number is 1-based");
        }
        this.number = number;
        this.bowler = Objects.requireNonNull(bowler, "bowler");
    }

    void addDelivery(Delivery delivery) {
        deliveries.add(Objects.requireNonNull(delivery, "delivery"));
    }

    public int legalDeliveries() {
        return (int) deliveries.stream().filter(d -> d.outcome().isLegalDelivery()).count();
    }

    public boolean isComplete() {
        return legalDeliveries() == 6;
    }

    public int getNumber() {
        return number;
    }

    public Player getBowler() {
        return bowler;
    }

    public List<Delivery> getDeliveries() {
        return List.copyOf(deliveries);
    }
}
