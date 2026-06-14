package in.neelporiya.phases.phase09collections;

import in.neelporiya.runner.Concept;

import java.util.*;

public class QueueDequeDemo implements Concept {
    @Override
    public String title() {
        return "Queue and Deque in Java";
    }

    @Override
    public String description() {
        return "Using Queue, Deque, and PriorityQueue in Java";
    }

    @Override
    public void run() {
        Queue<Integer> queue = new ArrayDeque<>();
        Deque<Integer> stack = new ArrayDeque<>();
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

        for (int i = 0; i < 5; ++i) {
            queue.offer(i);
            stack.push(i);
            minHeap.offer(i);
            maxHeap.offer(i);
        }

        System.out.println("Queue front : " + queue.poll());
        System.out.println("Stack top : " + stack.pop());
        System.out.println("MinHeap top: " + minHeap.poll());
        System.out.println("MaxHeap top: " + maxHeap.poll());

        try {
            List<Integer> list = List.of(1, 2, 3);
            for (Integer n : list) {
                if (n % 2 == 0) list.remove(n);
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Caught: " + e.getClass());
        }

        // fix
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        list.removeIf((n) -> n % 2 == 0);
        System.out.println(list);

        // does the same thing as above
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) it.remove();
        }

        try {
            List<Integer> immut = List.of(1, 2, 3);
            immut.add(5);
        } catch (UnsupportedOperationException e) {
            System.out.println("Caught: " + e.getClass());
        }
    }
}
