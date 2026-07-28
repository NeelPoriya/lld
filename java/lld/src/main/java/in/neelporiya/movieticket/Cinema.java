package in.neelporiya.movieticket;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cinema implements Identifiable {

    private final String id;
    private final String name;
    private final String cityId;
    private final String cityName;
    private final Map<String, Screen> screens;

    private Cinema(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = Objects.requireNonNull(builder.name, "name");
        this.cityId = Objects.requireNonNull(builder.cityId, "cityId");
        this.cityName = Objects.requireNonNull(builder.cityName, "cityName");
        this.screens = Collections.unmodifiableMap(new LinkedHashMap<>(builder.screens));
    }

    public static Builder builder(String id, String name, City city) {
        return new Builder(id, name, city.getId(), city.getName());
    }

    public static Builder builder(String id, String name, String cityId, String cityName) {
        return new Builder(id, name, cityId, cityName);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public List<Screen> getScreens() {
        return List.copyOf(screens.values());
    }

    public Screen getScreen(String screenId) {
        Screen screen = screens.get(screenId);
        if (screen == null) {
            throw new IllegalArgumentException("unknown screen " + screenId);
        }
        return screen;
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final String cityId;
        private final String cityName;
        private final Map<String, Screen> screens = new LinkedHashMap<>();

        private Builder(String id, String name, String cityId, String cityName) {
            this.id = id;
            this.name = name;
            this.cityId = cityId;
            this.cityName = cityName;
        }

        public Builder addScreen(Screen screen) {
            screens.put(screen.getId(), screen);
            return this;
        }

        public Cinema build() {
            return new Cinema(this);
        }
    }
}
