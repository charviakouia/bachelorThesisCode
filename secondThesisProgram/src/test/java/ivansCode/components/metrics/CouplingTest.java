package ivansCode.components.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CouplingTest {

    @Test
    void testNonDisjointDistance(){

        int distance = Coupling.distance(Set.of(1, 2, 3), Set.of(2, 3, 4));
        Assertions.assertEquals(2, distance);

    }

    @Test
    void testDisjointDistance(){

        int distance = Coupling.distance(Set.of(1, 2), Set.of(3, 4));
        Assertions.assertEquals(4, distance);

    }

    @Test
    void testEqualSetDistance(){

        int distance = Coupling.distance(Set.of(1, 2), Set.of(1, 2));
        Assertions.assertEquals(0, distance);

    }

}