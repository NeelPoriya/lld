package in.neelporiya.phases.phase10exceptions;

import in.neelporiya.runner.Concept;

import java.io.IOException;

public class ExceptionBasicsDemo implements Concept {
    @Override
    public String title() {
        return "Exceptions in Java";
    }

    @Override
    public String description() {
        return "Getting started with basics of exceptions, try/catch/finally " +
                "and checked and unchecked exceptions";
    }

    void printException(Exception e) {
        System.out.println("Caught: " + e.getClass() + " " + e.getMessage());
    }

    void throwsException () throws IOException {
        throw new IOException("This is a sample exception");
    }

    @Override
    public void run() {
        Integer n = null;

        try {
            System.out.println(10 * n);
        } catch (Exception e) {
            printException(e);
        }

        try {
            System.out.println(1 / 0);
        } catch (ArithmeticException e) {
            printException(e);
        }

        try {
            throwsException();
        } catch (IOException e) {
            printException(e);
        } finally {
            System.out.println("This block of code will always run.");
        }

        // Java has checked exceptions, meaning we check explicitly mention that a given
        // method will throw an exception; this is not present in C++/C#
    }
}
