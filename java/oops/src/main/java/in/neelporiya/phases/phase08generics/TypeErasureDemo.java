package in.neelporiya.phases.phase08generics;

import in.neelporiya.runner.Concept;

import java.util.ArrayList;
import java.util.List;

public class TypeErasureDemo implements Concept {
    @Override
    public String title() {
        return "Generics Type Erasure on runtime";
    }

    @Override
    public String description() {
        return "Java doesn't know the difference between List<Integer> and List<String> or any other List," +
                " because all of them are same after compiling the code";
    }

    private static class Stack<T> {
        private Object[] arr;
        private int size;
        public Stack(int capacity) {
            this.arr = new Object[capacity];
            this.size = 0;
        }

        public void push(T val) {
            arr[size++] = val;
        }

        @SuppressWarnings("unchecked")
        public T pop() {
            return (T) arr[--size];
        }
    }

    @Override
    public void run() {
        List<Integer> integerList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();

        System.out.println(integerList.getClass() == stringList.getClass());

        Stack<Integer> st = new Stack<>(10);
        st.push(1);
        st.push(2);
        st.push(3);

        System.out.println(st.pop());

        /*
        Restrictions:
        1. Can't test a parameterized type - runtime doesn't know it:
        if (x instanceof List<String>) {} // X - compile error; only `instanceof List<?> is allowed.

        2. Can't instantiate a type parameter - no runtime type to call `new` on:
        T t = new T();

        3. Can't create a generic array:
        T[] arr = new T[10];

        4. Can't overload on erased signature - both erase to same method
        void f(List<String> s) {}
        void f(List<Integer> i) {} // X <- both are same

        5. No static field of type T, can't catch a generic exception, etc.
         */
    }
}
