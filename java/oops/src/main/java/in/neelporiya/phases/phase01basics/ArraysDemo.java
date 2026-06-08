package in.neelporiya.phases.phase01basics;

import in.neelporiya.runner.Concept;

import java.util.Arrays;

public class ArraysDemo implements Concept {
    @Override
    public String title() {
        return "Arrays in Java";
    }

    @Override
    public String description() {
        return "Simple demo for showcasing arrays in java";
    }

    @Override
    public void run() {
        int[] a = new int[5];
        int[] b = {1, 2, 3};
        System.out.println(a.length);
        System.out.println(b.length);

        System.out.println("Printing arrays directly is useless. System.out.println(b); b = " + b);
        System.out.println(Arrays.toString(b));

        try {
            int x = b[10];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            Object[] arr = new String[3];
            arr[0] = "ok";
            arr[1] = 42; // compiles fine, but throws ArrayStoreException
        } catch (ArrayStoreException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        int[][] grid = new int[2][3];
        grid[0][1] = 9;
        System.out.println(Arrays.deepToString(grid));
    }
}
