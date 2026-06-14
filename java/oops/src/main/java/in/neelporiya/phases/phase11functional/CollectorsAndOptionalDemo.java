package in.neelporiya.phases.phase11functional;

import in.neelporiya.runner.Concept;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectorsAndOptionalDemo implements Concept {
    @Override
    public String title() {
        return "Collectors and Optional in Java";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public void run() {
        List<String> words = List.of("apple", "banana", "avocado", "cherry", "blueberry");

        Map<Character, List<String>> byFirst = words.stream().collect(Collectors.groupingBy(w -> w.charAt(0)));

        Map<Character, Long> countByFirst = words.stream().collect(Collectors.groupingBy(w -> w.charAt(0), Collectors.counting()));

        Map<Boolean, List<String>> byLength = words.stream().collect(Collectors.partitioningBy(w -> w.length() > 5));

        Map<String, Integer> lengths = words.stream().collect(Collectors.toMap(w -> w, String::length));

        System.out.println(byFirst);
        System.out.println(countByFirst);
        System.out.println(byLength);
        System.out.println(lengths);

        String text = "the cat sat on the mat the cat ran";
        Map<String, Long> freq = Arrays.stream(text.split(" "))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        freq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        Optional<String> found = words.stream().filter(w -> w.startsWith("z")).findFirst();
        System.out.println(found.isPresent());
        System.out.println(found.orElse("none"));
        found.ifPresent(System.out::println);
    }
}
