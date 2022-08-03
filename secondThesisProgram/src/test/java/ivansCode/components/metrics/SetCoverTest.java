package ivansCode.components.metrics;

import ivansCode.components.Matrix;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SetCoverTest {

    private static Matrix matrix;

    @BeforeAll
    static void init() {

        Set<Pair<String, Integer>> killRelation = Set.of(
                Pair.of("A", 1),
                Pair.of("A", 2),
                Pair.of("B", 2),
                Pair.of("B", 3),
                Pair.of("C", 1),
                Pair.of("C", 3),
                Pair.of("C", 6),
                Pair.of("D", 1)
        );
        Set<String> testNames = Set.of(
                "A", "B", "C", "D", "E"
        );
        Set<Integer> mutantIds = Set.of(
                1, 2, 3, 4, 5, 6
        );
        Set<Integer> equivalentMutants = Set.of(
                4, 5
        );
        matrix = new Matrix(killRelation, testNames, mutantIds, equivalentMutants);

    }

    @Test
    void testGreedyBigStep(){

        Set<String> minimumTests = SetCover.greedyBigStep(matrix.getTestCoverMap(), 2);

        Set<String> option1 = Set.of("A", "C");
        Set<String> option2 = Set.of("B", "C");
        Assertions.assertTrue(minimumTests.equals(option1) || minimumTests.equals(option2));

    }

}