package template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import user.template.Calculator;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalcSumTest {

    Calculator calculator;
    String numFilepath;

    @BeforeEach
    public void setUp() throws IOException {
        this.calculator = new Calculator();
        this.numFilepath = new ClassPathResource("numbers.txt").getURI().getPath();
    }

    @Test
    public void sumOfNumbers() throws IOException {
        assertEquals(calculator.calcSum(this.numFilepath), 10);
    }

    @Test
    public void multiplyOfNumbers() throws IOException {
        assertEquals(calculator.calcMultiply(this.numFilepath), 24);
    }

    @Test
    public void concatenate() throws IOException{
        assertEquals(calculator.concatenate(this.numFilepath), "1234");
    }

}
