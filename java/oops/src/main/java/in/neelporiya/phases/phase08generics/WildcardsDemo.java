package in.neelporiya.phases.phase08generics;

import in.neelporiya.runner.Concept;

import java.util.ArrayList;
import java.util.List;

public class WildcardsDemo implements Concept {
    @Override
    public String title() {
        return "Wildcards in Java";
    }

    @Override
    public String description() {
        return "wildcards are used in case when your method takes List<Integers> but " +
                "you also want to pass List<Number> (that's not allowed by default) unless " +
                "we use wildcards (?)";
    }

    @Override
    public void run() {
        // There are three types of wildcards
        // 1. List<?> // unknown type - read as Object, can't add anything except null
        // 2. List<? extends T> // T or a subtype - a producer you read T out of
        // 3. List<? super T> // T or a supertype - a consumer you put T into

        // the following is not allowed
        // List<Number> x = new ArrayList<Integer>(List.of(1, 2, 3));

        double sum = sumAll(new ArrayList<Double>(List.of(1.0, 2.0, 3.0)));
        System.out.println(sum);

        List<Integer> list = new ArrayList<Integer>(List.of(1, 2, 3));
        addInts(list);
        System.out.println(list);
    }

    // example of List<? extends T>
    double sumAll(List<? extends Number> nums) { // accepts List<Integer>, List<Double>, List<Number>
        double total = 0;
        for (Number n : nums) total += n.doubleValue(); // can read as a Number
        // nums.add(3); // can't ADD - compiler doesn't know the exact subtype
        return total;
    }

    // example of List<? super T>
    void addInts(List<? super Integer> dest) { // accepts List<Integer>, List<Number>, List<Object>
        dest.add(1);
        dest.add(2); // can add integers
        // Integer x = dest.get(0); // X <- read comes back as Object - unknown supertype
    }
}
