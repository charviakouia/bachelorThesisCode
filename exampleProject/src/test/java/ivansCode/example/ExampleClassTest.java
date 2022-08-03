package ivansCode.example;

import ivansCode.example.ExampleClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExampleClassTest {

    @Test
    void testAdd(){
        Assertions.assertEquals(5, ExampleClass.add(2, 3));
    }

    @Test
    void testConstant(){
        Assertions.assertEquals(5, ExampleClass.getConstant());
    }

}