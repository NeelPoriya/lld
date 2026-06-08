package in.neelporiya;

import in.neelporiya.phases.phase01basics.*;
import in.neelporiya.phases.phase02objects.*;
import in.neelporiya.phases.phase03encapsulation.*;
import in.neelporiya.runner.*;

public class Main {
    public static void main(String[] args) {
        ConceptRegistry conceptRegistry = new ConceptRegistry();
        conceptRegistry.register(Phase.PHASE_01_BASICS, new PrimitiveTypesDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new WrapperBoxingDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new StringDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new ArraysDemo());

        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new ClassBasicsDemo());
        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new RecordsDemo());
        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new ReferenceSemanticsDemo());

        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new ImmutabilityDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new BuilderDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new NestedClassDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new SealedClassesDemo());

        MenuRunner menuRunner = new MenuRunner();
        // menuRunner.start(conceptRegistry);

        menuRunner.start(new SealedClassesDemo());
    }
}
