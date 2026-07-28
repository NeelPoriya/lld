package in.neelporiya.trafficsignal;

/** // DESIGN PATTERN: State — concrete RED state; RED transitions back to GREEN. */
public final class RedSignalState implements SignalState {

    private final SignalDurations durations;

    public RedSignalState(SignalDurations durations) {
        this.durations = durations;
    }

    @Override
    public SignalColor color() {
        return SignalColor.RED;
    }

    @Override
    public java.time.Duration duration() {
        return durations.red();
    }

    @Override
    public SignalState next() {
        return new GreenSignalState(durations);
    }
}
