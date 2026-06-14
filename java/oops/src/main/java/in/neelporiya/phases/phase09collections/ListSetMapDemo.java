package in.neelporiya.phases.phase09collections;

import in.neelporiya.runner.Concept;

import java.util.*;

public class ListSetMapDemo implements Concept {
    @Override
    public String title() {
        return "Collection in Java (List, Map, Set)";
    }

    @Override
    public String description() {
        return "Collections are the things which we use daily, these are implementations of " +
                "most widely used data structures in programming";
    }

    @Override
    public void run() {
        // Hash* -> no order (fastest, O(1))
        // Tree* -> sorted   (O(log n))
        // Linked* -> insertion order (O(1), slight overhead)

        List<String> starter = List.of("banana", "apple", "cherry", "apple");
        // C# HashSet, C++ std::unordered_set
        HashSet<String> hashSet = new HashSet<>(starter);
        // C# SortedSet, C++ std::set
        TreeSet<String> treeSet = new TreeSet<>(starter);
        // C# X, C++ X
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(starter);

        System.out.println(hashSet);
        System.out.println(treeSet);
        System.out.println(linkedHashSet);

        Map<String, Integer> start = Map.of("Apple", 1, "Cherry", 3, "Banana", 2);
        HashMap<String, Integer> hashMap = new HashMap<>(start);
        // C# SortedDictionary<K, V>, C++ std::map
        TreeMap<String, Integer> treeMap = new TreeMap<>(start);
        // C# OrderedDictionary
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>(start);

        System.out.println(hashMap);
        System.out.println(treeMap);
        System.out.println(linkedHashMap);

        // C# List<T>, C++ std::vector
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        System.out.println(list.get(2));
        System.out.println(list.contains(3));
        System.out.println(list.remove(1));
    }
}
