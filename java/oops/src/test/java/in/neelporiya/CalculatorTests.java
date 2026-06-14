package in.neelporiya;

import in.neelporiya.phases.phase14tests.Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTests {
    Calculator calc;

    @BeforeEach
    void setUp() { calc = new Calculator(); }

    @Test
    @DisplayName("adds two positive numbers")
    void addNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> calc.divide(1, 0));
    }

    @Test
    void multipleChecks() {
        assertAll(
                () -> assertEquals(4, calc.add(2, 2)),
                () -> assertTrue(calc.add(1, 1) > 0)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6, 100})
    void allEven(int n) {
        assertEquals(0, n % 2);
    }
}
