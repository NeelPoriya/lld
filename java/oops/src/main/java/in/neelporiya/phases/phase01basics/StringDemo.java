package in.neelporiya.phases.phase01basics;

import in.neelporiya.runner.Concept;

public class StringDemo implements Concept {
    @Override
    public String title() {
        return "Strings in Java";
    }

    @Override
    public String description() {
        return "Program to demonstrate strings are immutable in Java.";
    }

    @Override
    public void run() throws Exception {
        String a = "hello";
        String b = "hello";
        System.out.println("String a = \"hello\"; String b = \"hello\"; a == b: " + (a == b) + ". Because both point to the SAME pooled literal"); // true - both point to the SAME pooled literal

        String c = new String("hello");
        System.out.println("String c = new String(\"hello\"); a == c: " + (a == c));
        System.out.println("a.equals(c): " + a.equals(c));

        System.out.println("String are immutable, and some operations returns a new string, like toUpperCase");
        String s = "hello";
        s.toUpperCase();
        System.out.println("After s.toUpperCase(); s = " + s);
        s = s.toUpperCase();
        System.out.println("After reassigning s = s.toUpperCase(); s = " + s);

        System.out.println("StringBuilder is used for mutable strings");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; ++i) sb.append(i).append(",");
        System.out.println(sb);

        String msg = String.format("%s is %d years old", "Java", 30);
        String json = """
                {
                    "lang": "Java"
                }
                """;
        System.out.println("String formatting: " + msg);
        System.out.println("Multi-line block: " + json);
        System.out.println("Another Gotcha: String uses .length() method, while arrays use .length field");
        System.out.println("\"hello\".length() = " + "hello".length());
        int[] arr = {1, 2, 3};
        System.out.println("arr.length = " + arr.length);
    }
}
