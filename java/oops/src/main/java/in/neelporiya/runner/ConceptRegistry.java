package in.neelporiya.runner;

import java.util.*;

public class ConceptRegistry {
    private final Map<Phase, List<Concept>> phaseListMap = new HashMap<>();

    public void register(Phase phase, Concept concept) {
        phaseListMap
                .computeIfAbsent(phase, p -> new ArrayList<>())
                .add(concept);
    }

    public List<Concept> getConcepts(Phase phase) {
        return phaseListMap.getOrDefault(phase, List.of());
    }
}
