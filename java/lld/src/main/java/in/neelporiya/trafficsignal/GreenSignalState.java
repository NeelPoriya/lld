package in.neelporiya.trafficsignal;

/** // DESIGN PATTERN: State — concrete GREEN state; GREEN transitions to YELLOW. */
public final class GreenSignalState implements SignalState {

    private final SignalDurations durations;

    public GreenSignalState(SignalDurations durations) {
        this.durations = durations;
    }

    @Override
    public SignalColor color() {
        return SignalColor.GREEN;
    }

    @Override
    public java.time.Duration duration() {
        return durations.green();
    }

    @Override
    public SignalState next() {
        return new YellowSignalState(durations);
    }
}
