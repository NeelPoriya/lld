package in.neelporiya.phases.phase06abstraction;

import in.neelporiya.runner.Concept;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FunctionalInterfacesDemo implements Concept {
    @Override
    public String title() {
        return "Functional interfaces in Java";
    }

    @Override
    public String description() {
        return "functional interfaces are single method interface which are used to power lambda functions";
    }

    @FunctionalInterface
    interface Calculator {
        int apply(int a, int b);
    }

//    Wrong way, cannot have more than 1 method
//    @FunctionalInterface
//    interface Temp {
//        int apply(int a);
//        int supply(int b);
//    }

    @Override
    public void run() {
        Calculator add = (a, b) -> a + b;
        Calculator mul = (a, b) -> a * b;

        System.out.println(add.apply(1, 2));
        System.out.println(mul.apply(2, 3));

        Predicate<Integer> isOdd = n -> n % 2 == 1;
        Function<Integer, String> toString = n -> n.toString();
        Supplier<String> greet = () -> "hi";
        Consumer<String> printer = System.out::println;

        boolean result = isOdd.test(5);
        String str = toString.apply(12345);
        String message = greet.get();
        printer.accept(String.valueOf(result));
        printer.accept(str);
        printer.accept(message);
    }
}
