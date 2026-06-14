package in.neelporiya.phases.phase11functional;

import in.neelporiya.runner.Concept;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamBasicsDemo implements Concept {
    @Override
    public String title() {
        return "Streams in Java";
    }

    @Override
    public String description() {
        return "Streams takes in a source, performs a set of intermediate operations and then produces a final output" +
                " and if the final output step is missed, then it doesn't bother running the intermediate steps either";
    }

    @Override
    public void run() {
        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        List<Integer> result = nums.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .collect(Collectors.toList());

        System.out.println(result);

        Integer sum = nums.stream().map(n -> n * n).reduce(0, Integer::sum);
        System.out.println(sum);

        nums.stream().filter(n -> {
            // nothing prints - no terminal op
            System.out.println("Running for n : " + n);
            return n % 2 == 0;
        });

        try {
            Stream<Integer> s = nums.stream();
            s.count();
            s.count();
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getClass() + " " + e.getMessage());
        }

        // LINQ analogy
        // filter -> Where
        // map -> Select
        // reduce -> Aggregate
        // collect(toList()) -> ToList()
        // flatMap -> SelectMany
        // sorted -> OrderBy
        // count -> Count
        // Stream<T> ~~ IEnumerable<T>
    }
}
