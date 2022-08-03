package ivansCode.components.metrics;

import ivansCode.components.Matrix;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MutantSetTest {

    private static Matrix matrixA;
    private static Matrix matrixB;

    @BeforeAll
    static void init(){

        Set<Pair<String, Integer>> killRelationA = Set.of(
                Pair.of("A", 1),
                Pair.of("A", 2),
                Pair.of("B", 2),
                Pair.of("B", 3),
                Pair.of("C", 1),
                Pair.of("C", 3),
                Pair.of("C", 6),
                Pair.of("D", 1)
        );
        Set<String> testNamesA = Set.of(
                "A", "B", "C", "D", "E"
        );
        Set<Integer> mutantIdsA = Set.of(
                1, 2, 3, 4, 5, 6
        );
        Set<Integer> equivalentMutantsA = Set.of(
                4, 5
        );
        matrixA = new Matrix(killRelationA, testNamesA, mutantIdsA, equivalentMutantsA);

        Set<Pair<String, Integer>> killRelationB = Set.of(
                Pair.of("D", 7),
                Pair.of("A", 8),
                Pair.of("B", 8),
                Pair.of("C", 8),
                Pair.of("E", 8)
        );
        Set<String> testNamesB = Set.of(
                "A", "B", "C", "D", "E"
        );
        Set<Integer> mutantIdsB = Set.of(
                7, 8
        );
        Set<Integer> equivalentMutantsB = Set.of(
        );
        matrixB = new Matrix(killRelationB, testNamesB, mutantIdsB, equivalentMutantsB);

    }

    @Test
    void testCalculatingDisjointMutantSet(){

        Set<Integer> djm = MutantSet.disjointMutantSet(matrixA.getKillMap());
        Assertions.assertEquals(Set.of(2, 6), djm);

    }

    @Test
    void testSubsumption(){

        double subsumptionA = MutantSet.subsumption(matrixA.getKillMap(), matrixB.getKillMap());
        double subsumptionB = MutantSet.subsumption(matrixB.getKillMap(), matrixA.getKillMap());

        Assertions.assertEquals(0.5, subsumptionA, 0.00001);
        Assertions.assertEquals(0, subsumptionB, 0.00001);

    }

}