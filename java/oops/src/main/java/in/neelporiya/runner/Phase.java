package in.neelporiya.runner;

public enum Phase {
    PHASE_01_BASICS("Phase 1 · Language Basics"),
    PHASE_02_OBJECTS("Phase 2 · Classes & Objects"),
    PHASE_03_ENCAPSULATION ("Phase 3 · Encapsulation"),
    PHASE_04_INHERITANCE ("Phase 4 · Inheritance"),
    PHASE_05_POLYMORPHISM ("Phase 5 · Polymorphism"),
    PHASE_06_ABSTRACTION ("Phase 6 · Abstraction"),
    PHASE_07_COMPOSITION ("Phase 7 · Composition"),
    PHASE_08_GENERICS ("Phase 8 · Generics"),
    PHASE_09_COLLECTIONS ("Phase 9 · Collections"),
    PHASE_10_EXCEPTIONS("Phase 10 · Exceptions"),
    PHASE_11_FUNCTIONAL("Phase 11 · Functional"),
    PHASE_12_CONCURRENCY("Phase 12 · Concurrency"),
    PHASE_13_IO("Phase 13 · IO");


    private final String displayName;

    Phase(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
