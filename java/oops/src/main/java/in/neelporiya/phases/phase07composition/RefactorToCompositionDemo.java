package in.neelporiya.phases.phase07composition;

import in.neelporiya.runner.Concept;

public class RefactorToCompositionDemo implements Concept {
    @Override
    public String title() {
        return "Composition Demo";
    }

    @Override
    public String description() {
        return "this one illustrates when to use composition when inheritance fails";
    }

    @FunctionalInterface
    private interface Role {
        String responsibilities();
    }

    private static class ManagerRole implements Role {
        @Override
        public String responsibilities() {
            return "Handles people";
        }
    }

    private static class DirectorRole implements Role {
        @Override
        public String responsibilities() {
            return "Handles entire charters...";
        }
    }

    private static class Employee {
        private final String name;
        private Role role;
        public Employee(String name, Role role) {
            this.name = name;
            this.role = role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public String describe() {
            return name + ": " + role.responsibilities();
        }
    }

    @Override
    public void run() {
        Employee emp1 = new Employee("Ramesh", new ManagerRole());
        System.out.println(emp1.describe());

        emp1.setRole(new DirectorRole());
        System.out.println(emp1.describe());

        emp1.setRole(() -> "leads different projects");
        System.out.println(emp1.describe());
    }
}
