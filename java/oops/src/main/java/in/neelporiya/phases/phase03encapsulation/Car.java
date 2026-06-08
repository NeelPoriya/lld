package in.neelporiya.phases.phase03encapsulation;

public class Car {
    private final String model =  "Tesla";
    public class Dashboard {
        void show() {
            System.out.println(model);
            System.out.println(Car.this.model);
        }
    }
}
