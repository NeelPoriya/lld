package in.neelporiya.phases.phase10exceptions;

import in.neelporiya.runner.Concept;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class TryWithResources implements Concept {
    @Override
    public String title() {
        return "Exception with resources in Java";
    }

    @Override
    public String description() {
        return "AutoCloseable closes automatically anything that is declared inside a try block, we can throw custom " +
                "exceptions without losing existing stack trace";
    }

    public static class Resource implements AutoCloseable {
        private final String name;
        public Resource(String name) {
            this.name = name;
            System.out.println("Opened Resource");
        }
        @Override public void close() {
            System.out.println("Close");
        }
    }

    public static class CustomException extends Exception {
        public CustomException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    @Override
    public void run() {
        try (Resource r = new Resource("Test")) {
            throw new IOException("Okay");
        } catch (IOException e) {
            System.out.println("Caught: " + e.getClass() + " " + e.getMessage() + ". Closed the scanner as well");
        }

        try {
            double random = Math.random();
            if (random < 0.5) {
                throw new NullPointerException("Null Pointer Exception");
            } else {
                throw new ArithmeticException("Divide by Zero error");
            }
        } catch (NullPointerException | ArithmeticException e) {
            System.out.println("Caught: " + e.getClass() + " " + e.getMessage());
        }

        try {
            try {
                Integer.parseInt("abcd");
            } catch (NumberFormatException e) {
                throw new CustomException("Not a valid number", e);
            }
        } catch (CustomException e) {
            System.out.println("Caught: " + e.getClass() + " " + e.getMessage() + " " + e.getCause());
        }
    }
}
