package in.neelporiya.runner;

public enum Phase {
    PHASE_01_BASICS("Phase 1 · Language Basics"),
    PHASE_02_OBJECTS("Phase 2 · Classes & Objects"),
    PHASE_03_ENCAPSULATION ("Phase 3 · Encapsulation");

    private final String displayName;

    Phase(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
