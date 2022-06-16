package ivansCode.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExampleTest {

    @Test
    public void testAdd(){
        Assertions.assertEquals(ExampleClass.add(2, 3), 5);
    }

    @Test
    public void testSubtract(){
        Assertions.assertEquals(ExampleClass.subtract(2, 3), -1);
    }

    @Test
    public void testMultiply(){
        Assertions.assertEquals(ExampleClass.multiply(2, 3), 6);
    }

    @Test
    public void testDivide(){
        Assertions.assertEquals(ExampleClass.divide(6, 3), 2);
    }

    @Test
    public void testDivideByZero(){
        Assertions.assertThrows(Exception.class, () -> ExampleClass.divide(2, 0));
    }

}
