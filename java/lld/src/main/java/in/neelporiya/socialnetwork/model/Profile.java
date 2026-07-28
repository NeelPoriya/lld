package in.neelporiya.socialnetwork.model;

import java.util.Objects;

public record Profile(String displayName, String bio, String city) {

    public Profile {
        Objects.requireNonNull(displayName, "displayName");
        bio = bio == null ? "" : bio;
        city = city == null ? "" : city;
    }

    public static Profile of(String displayName, String bio, String city) {
        return new Profile(displayName, bio, city);
    }
}
