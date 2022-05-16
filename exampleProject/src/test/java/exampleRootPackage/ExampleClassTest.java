package exampleRootPackage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExampleClassTest {

    @Test
    public void testAdd(){

        Assertions.assertEquals(ExampleClass.add(1, 3), 4);

    }

    @Test
    public void uselessTest(){

        int sillyExpression = 73 % 10 + 3;
        Assertions.assertEquals(sillyExpression, 6);

    }

}
