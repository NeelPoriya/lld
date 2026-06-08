package in.neelporiya.phases.phase01basics;

import in.neelporiya.runner.Concept;

public class PrimitiveTypesDemo implements Concept {

    @Override
    public String title() {
        return "Primitive Types Demo";
    }

    @Override
    public String description() {
        return "Simple program to test all the various primitive data types supported by Java";
    }

    @Override
    public void run() {
        System.out.println("Printing each(8) primitive type.");

        System.out.println("1. Byte");
        byte b = Byte.MIN_VALUE;
        System.out.println("Min Value: " + b + " and Max Value: " + Byte.MAX_VALUE);

        System.out.println("2. Short");
        short s = Short.MIN_VALUE;
        System.out.println("Min Value: " + s + " and Max Value: " + Short.MAX_VALUE);

        System.out.println("3. Integer");
        int i = Integer.MIN_VALUE;
        System.out.println("Min Value: " + i + " and Max Value: " + Integer.MAX_VALUE);

        System.out.println("4. Long");
        long l = Long.MIN_VALUE;
        System.out.println("Min Value: " + l + " and Max Value: " + Long.MAX_VALUE);

        System.out.println("5. Float");
        float f = Float.MIN_VALUE;
        System.out.println("Smallest positive value: " + f + " and Max Value: " + Float.MAX_VALUE);

        System.out.println("6. Double");
        double d = Double.MIN_VALUE;
        System.out.println("Smallest positive value: " + d + " and Max Value: " + Double.MAX_VALUE);

        System.out.println("7. Boolean");
        boolean bl = false;
        System.out.println("False: " + Boolean.FALSE + " and True: " + Boolean.TRUE);

        System.out.println("8. Character");
        char c = Character.MIN_VALUE;
        System.out.println("Min Value: " + c + " and Max Value: " + Character.MAX_VALUE);

        System.out.println("Gotchas!!");
        int max = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE + 1: " + (max + 1)); // wraps to Integer.MIN_VALUE

        System.out.println("7 / 2 = " + (7 / 2));
        System.out.println("7.0 / 2 = " + (7.0 / 2));

        // System.out.println(1 / 0); // throws Arithmetic Exception
        System.out.println("1.0 / 0 = " + 1.0 / 0); // Infinity
        System.out.println("0.0 / 0.0 = " + 0.0 / 0.0); // NaN

        c = 'A';
        System.out.println("(int) c: " + (int)c);
        System.out.println("(char)(c + 1): " + (char)(c + 1));

        // int x = b; // won't compile
        // if (1) { } // won't compile
        System.out.println(0.1 + 0.2); // 0.30000000000000004

        long big = 100;
        int small = (int)3.99;

        long population = 8_000_000_000L;
        int hex = 0xFF;
        int binary = 0b1010;
        float pi = 3.14f;

        System.out.println("(int) 3.99 = " + small);
        System.out.println("population = " + population);
        System.out.println("0xFF = " + hex + ", 0b1010 = " + binary);
        System.out.println("3.14f = " + pi);
    }
}
