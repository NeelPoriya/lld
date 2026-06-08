package in.neelporiya.runner;

import java.util.List;
import java.util.Scanner;

public class MenuRunner {
    public void start(ConceptRegistry conceptRegistry) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // 1) Pick a phase
            Phase[] phases = Phase.values();
            System.out.println("\nChoose a phase:");
            for (int i = 0; i < phases.length; ++i) {
                System.out.println((i + 1) + ". " + phases[i].displayName());
            }
            System.out.println("0. Exit");
            int phaseChoice = readChoice(scanner, 0, phases.length);
            if (phaseChoice == 0) {
                System.out.println("Goodbye!");
                break;
            }
            Phase chosen = phases[phaseChoice - 1];

            // 2) Pick a concept within that phase
            List<Concept> concepts = conceptRegistry.getConcepts(chosen);
            if (concepts.isEmpty()) {
                System.out.println("No concepts registered for this phase yet.");
                return;
            }

            System.out.println("\nChoose a concept:");
            for (int i = 0; i < concepts.size(); ++i) {
                System.out.println((i + 1) + ". " + concepts.get(i).title());
            }
            System.out.println("0. Back");

            int conceptChoice = readChoice(scanner, 0, concepts.size());
            if (conceptChoice == 0) {
                continue;
            }
            Concept concept = concepts.get(conceptChoice - 1);

            // 3) Run it
            System.out.println("\n=== " + concept.title() + " ===");
            System.out.println(concept.description());
            try {
                concept.run();
            } catch (Exception e) {
                System.out.println("Demo threw an exception: " + e.getMessage());
            }
            // loop repeats -> phase menu shows again
        }
    }

    /*
        TODO: For smoke test, remove when everything is done.
     */
    public void start(Concept concept) {
        try {
            concept.run();
        } catch (Exception ex) {
            System.out.println("Code threw an exception: " + ex.getMessage());
        }
    }

    private int readChoice(Scanner scanner, int min, int max) {
        while (true) {
            System.out.println("Enter choice (" + min + "-" + max + "): ");

            if (!scanner.hasNextInt()) {
                System.out.println("That's not a number. Try again.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < min || choice > max) {
                System.out.println("Out of range. Pick " + min + "-" + max + ".");
                continue;
            }

            return choice;
        }
    }
}
