package in.neelporiya.phases.phase08generics;

import in.neelporiya.runner.Concept;

import java.util.List;

public class GenericsBasicsDemo implements Concept {
    @Override
    public String title() {
        return "Getting started with generics";
    }

    @Override
    public String description() {
        return "generics gives you a way to specify types dynamically to methods/classes";
    }

    private static class Pair<A, B> {
        private final A first;
        private final B second;
        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A first() { return first; }
        public B second() { return second; }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    private <T> T firstOf(List<T> list) {
        return list.getFirst();
    }

    private <T extends Number> double sum(List<T> list) {
        double total = 0;
        for (T n : list) total += n.doubleValue();
        return total;
    }

    @Override
    public void run() {
        Pair<Integer, String> p = new Pair<>(123, "123");
        Integer first = firstOf(List.of(1, 2, 3));
        System.out.println(first);
        System.out.println(p);
        System.out.println(sum(List.of(1.0, 1, 2)));

//        It's unsafe to not provide type when creating generic objects.
//        List l = new ArrayList<>();
//        l.add("Hello");
//        l.add(50);
//        System.out.println(l.get(1));
    }
}
