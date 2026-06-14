package in.neelporiya.phases.phase06abstraction;

import in.neelporiya.runner.Concept;

public class DefaultMethodsDemo implements Concept {
    @Override
    public String title() {
        return "Default methods in Interface";
    }

    @Override
    public String description() {
        return "we can write default/private/static methods in interfaces, and in case of methods with same" +
                "names in different interfaces implemented by a child class, java enforces the child class" +
                "to override that common method, otherwise it won't compile";
    }

    public interface Test1 {
        default void doSomething() {
            System.out.println("This does something");
        }

        static void printSomething() {
            System.out.println("Printing a random number: " + (int) (Math.random() * 100));
        }
    }

    public interface Test2 {
        default void doSomething() {
            System.out.println("This also does some things");
        }
    }

    public static class ConcreteTest implements Test1, Test2 {

        @Override
        public void doSomething() {
            // if we don't make ConcreteTest static, then it silently carries an instance of the parent
            // class with itself. General, rule of thumb is, make nested class static unless you need the
            // outer instance.
            // DefaultMethodsDemo.this.run();
            Test1.super.doSomething();
            Test2.super.doSomething();
            System.out.println("This is the child class doing something");
        }
    }

    @Override
    public void run() {
        ConcreteTest test = new ConcreteTest();
        test.doSomething();
        Test1.printSomething();
    }
}
