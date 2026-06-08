package in.neelporiya.phases.phase04inheritance;

import in.neelporiya.runner.Concept;

public class FinalAndOverloadingDemo implements Concept {
    @Override
    public String title() {
        return "Final and Overloading Class and Methods in Java";
    }

    @Override
    public String description() {
        return "final classes cannot be further inherited, and final methods cannot be overridden.\nBoxing has " +
                "interesting effects on method overloading, long > Integer > int..., this is because" +
                "of backward compatibility as Integer and int... were added in Java 5";
    }

    void print(long x) {
        System.out.println("Long " + x);
    }

    void print(Integer x) {
        System.out.println("Integer " + x);
    }

    void print(int... x) {
        System.out.println("int... " + x);
    }

    void g(String s) {
        System.out.println("String " + s);
    }

    void g(Object s) {
        System.out.println("Object " + s);
    }

    @Override
    public void run() {
        // This won't work
        // public final class Parent {}
        // public class Child extends Parent {} <- Wrong

        // public final void test()
        // @Override public final void test() <- Wrong

        print(5);
        // most specific reference type is picked,
        // and in case of two specific types, we get a compile time error.
        g(null);
    }
}
