package in.neelporiya.phases.phase01basics;

import in.neelporiya.runner.Concept;

import java.util.ArrayList;

public class ControlFlowDemo implements Concept {
    @Override
    public String title() {
        return "Conditionals in Java";
    }

    @Override
    public String description() {
        return "Small demo of how conditions work in Java";
    }

    @Override
    public void run() throws Exception {
        int num = 3;
        String name = switch (num) {
            case 1, 7 -> "weekend-ish";
            case 2, 3, 4, 5, 6 -> "weekday";
            default -> "unknown";
        };
        System.out.println("Name is " + name);

        int score = 85;
        String grade = switch (score / 10) {
            case 10, 9 -> "A";
            case 8 -> {
                System.out.println("computing...");
                yield "B";
            }
            default -> "F";
        };
        System.out.println("Grade is " + grade);

        var count = 10;
        var language = "Java";
        var list = new ArrayList<String>();

        System.out.println("var count = 10; count = " + count);
        System.out.println("var language = \"Java\"; language = " + language);
        System.out.println("var list = new ArrayList<String>(); list = " + list);
    }
}
