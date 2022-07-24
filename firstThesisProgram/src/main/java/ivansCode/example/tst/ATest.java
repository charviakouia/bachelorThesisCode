package ivansCode.example.tst;

import ivansCode.example.src.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ATest {

    @Test
    void testAdd(){
        Assertions.assertEquals(5, A.add(2, 3));
    }

}
