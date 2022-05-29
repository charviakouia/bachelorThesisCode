package ivansCode.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExampleTest {

    @Test
    public void testAdd(){
        Assertions.assertEquals(new ExampleClass().add(4, 5), 9);
    }

}
