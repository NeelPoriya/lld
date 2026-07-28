package in.neelporiya.trafficsignal;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple observer a UI could poll/render. */
public class DisplayBoard implements SignalObserver {

    private final Map<Direction, SignalColor> latestColors = new ConcurrentHashMap<>();

    @Override
    public void onStateChanged(SignalChangeEvent event) {
        latestColors.put(event.direction(), event.newColor());
    }

    public SignalColor colorFor(Direction direction) {
        return latestColors.get(direction);
    }

    public Map<Direction, SignalColor> snapshot() {
        Map<Direction, SignalColor> snapshot = new EnumMap<>(Direction.class);
        snapshot.putAll(latestColors);
        return Collections.unmodifiableMap(snapshot);
    }
}
