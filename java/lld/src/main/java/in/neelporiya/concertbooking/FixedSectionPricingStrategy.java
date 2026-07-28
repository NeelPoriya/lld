package in.neelporiya.concertbooking;

public class FixedSectionPricingStrategy implements SectionPricingStrategy {

    @Override
    public long priceCents(Concert concert, Section section, Seat seat) {
        return section.getBasePriceCents();
    }
}
