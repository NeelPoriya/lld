package in.neelporiya.phases.phase03encapsulation;

public class User {
    private final String name;
    private final String email;
    private final int age;

    private User(Builder b) {
        this.name = b.name;
        this.email = b.email;
        this.age = b.age;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public static class Builder {
        private final String name;
        private String email;
        private int age;

        Builder(String name) {
            this.name = name;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public String toString() {
        return "User[name=" + name + ",email=" + email + ",age=" + age + "]";
    }
}
