package in.neelporiya.trafficsignal;

/** // DESIGN PATTERN: Observer — display boards/loggers subscribe to state changes. */
public interface SignalObserver {

    void onStateChanged(SignalChangeEvent event);
}
