package in.neelporiya;

import in.neelporiya.phases.phase01basics.*;
import in.neelporiya.phases.phase02objects.*;
import in.neelporiya.phases.phase03encapsulation.*;
import in.neelporiya.phases.phase04inheritance.*;
import in.neelporiya.phases.phase05polymorphism.*;
import in.neelporiya.phases.phase06abstraction.*;
import in.neelporiya.phases.phase07composition.*;
import in.neelporiya.phases.phase08generics.*;
import in.neelporiya.phases.phase09collections.*;
import in.neelporiya.phases.phase10exceptions.*;
import in.neelporiya.phases.phase11functional.*;
import in.neelporiya.phases.phase12concurrency.*;
import in.neelporiya.phases.phase13io.*;
import in.neelporiya.runner.*;

public class Main {
    static void main(String[] args) {
        ConceptRegistry conceptRegistry = new ConceptRegistry();
        conceptRegistry.register(Phase.PHASE_01_BASICS, new PrimitiveTypesDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new WrapperBoxingDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new ControlFlowDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new StringDemo());
        conceptRegistry.register(Phase.PHASE_01_BASICS, new ArraysDemo());

        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new ClassBasicsDemo());
        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new RecordsDemo());
        conceptRegistry.register(Phase.PHASE_02_OBJECTS, new ReferenceSemanticsDemo());

        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new ImmutabilityDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new BuilderDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new NestedClassDemo());
        conceptRegistry.register(Phase.PHASE_03_ENCAPSULATION, new SealedClassesDemo());

        conceptRegistry.register(Phase.PHASE_04_INHERITANCE, new InheritanceDemo());
        conceptRegistry.register(Phase.PHASE_04_INHERITANCE, new FinalAndOverloadingDemo());

        conceptRegistry.register(Phase.PHASE_05_POLYMORPHISM, new CastingDemo());
        conceptRegistry.register(Phase.PHASE_05_POLYMORPHISM, new SwitchPatternMatchingDemo());
        conceptRegistry.register(Phase.PHASE_05_POLYMORPHISM, new CovariantReturnDemo());

        conceptRegistry.register(Phase.PHASE_06_ABSTRACTION, new AbstractVsInterfaceDemo());
        conceptRegistry.register(Phase.PHASE_06_ABSTRACTION, new DefaultMethodsDemo());
        conceptRegistry.register(Phase.PHASE_06_ABSTRACTION, new ComparableVsComparatorDemo());
        conceptRegistry.register(Phase.PHASE_06_ABSTRACTION, new FunctionalInterfacesDemo());

        conceptRegistry.register(Phase.PHASE_07_COMPOSITION, new CompositionDemo());
        conceptRegistry.register(Phase.PHASE_07_COMPOSITION, new RefactorToCompositionDemo());

        conceptRegistry.register(Phase.PHASE_08_GENERICS, new GenericsBasicsDemo());
        conceptRegistry.register(Phase.PHASE_08_GENERICS, new TypeErasureDemo());
        conceptRegistry.register(Phase.PHASE_08_GENERICS, new WildcardsDemo());

        conceptRegistry.register(Phase.PHASE_09_COLLECTIONS, new ListSetMapDemo());
        conceptRegistry.register(Phase.PHASE_09_COLLECTIONS, new QueueDequeDemo());

        conceptRegistry.register(Phase.PHASE_10_EXCEPTIONS, new ExceptionBasicsDemo());
        conceptRegistry.register(Phase.PHASE_10_EXCEPTIONS, new TryWithResources());

        conceptRegistry.register(Phase.PHASE_11_FUNCTIONAL, new StreamBasicsDemo());
        conceptRegistry.register(Phase.PHASE_11_FUNCTIONAL, new CollectorsAndOptionalDemo());

        conceptRegistry.register(Phase.PHASE_12_CONCURRENCY, new ThreadsAndExecutorsDemo());
        conceptRegistry.register(Phase.PHASE_12_CONCURRENCY, new BlockingQueueDemo());
        conceptRegistry.register(Phase.PHASE_12_CONCURRENCY, new CompletableFutureDemo());

        conceptRegistry.register(Phase.PHASE_13_IO, new FilesAndPathsDemo());
        conceptRegistry.register(Phase.PHASE_13_IO, new DateTimeDemo());
        conceptRegistry.register(Phase.PHASE_13_IO, new HttpClientDemo());

        MenuRunner menuRunner = new MenuRunner();
         menuRunner.start(conceptRegistry);

//        menuRunner.start(new HttpClientDemo());
    }
}
