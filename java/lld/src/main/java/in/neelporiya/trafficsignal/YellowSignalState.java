package in.neelporiya.trafficsignal;

/** // DESIGN PATTERN: State — concrete YELLOW state; YELLOW transitions to RED. */
public final class YellowSignalState implements SignalState {

    private final SignalDurations durations;

    public YellowSignalState(SignalDurations durations) {
        this.durations = durations;
    }

    @Override
    public SignalColor color() {
        return SignalColor.YELLOW;
    }

    @Override
    public java.time.Duration duration() {
        return durations.yellow();
    }

    @Override
    public SignalState next() {
        return new RedSignalState(durations);
    }
}
