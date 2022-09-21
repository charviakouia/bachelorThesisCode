package ivansCode.components.metrics;

import ivansCode.components.Matrix;
import ivansCode.utils.MatrixLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                Pair.of("D", 1),
                Pair.of("A", 11),
                Pair.of("D", 11),
                Pair.of("E", 11)
        );
        Set<String> testNamesA = Set.of(
                "A", "B", "C", "D", "E"
        );
        Set<Integer> mutantIdsA = Set.of(
                1, 2, 3, 4, 5, 6, 11
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
                Pair.of("E", 8),
                Pair.of("C", 9),
                Pair.of("E", 10)
        );
        Set<String> testNamesB = Set.of(
                "A", "B", "C", "D", "E"
        );
        Set<Integer> mutantIdsB = Set.of(
                7, 8, 9, 10
        );
        Set<Integer> equivalentMutantsB = Set.of(
        );
        matrixB = new Matrix(killRelationB, testNamesB, mutantIdsB, equivalentMutantsB);

    }

    @Test
    void testCalculatingDisjointMutantSet(){

        Set<Integer> djm = MutantSet.disjointMutantSet(matrixA.getKillMap());
        Assertions.assertEquals(Set.of(2, 6, 11), djm);

    }

    @Test
    void testSubsumption(){

        Map<Integer, Set<String>> mutantUniverseMap = new HashMap<>();
        mutantUniverseMap.putAll(matrixA.getKillMap());
        mutantUniverseMap.putAll(matrixB.getKillMap());
        Set<Set<String>> disjointUniverse = MutantSet.disjointMutantSet(mutantUniverseMap).stream()
                .map(mutantUniverseMap::get).collect(Collectors.toSet());

        double subsumptionA = MutantSet.subsumption(matrixA.getKillMap(), matrixB.getKillMap(), disjointUniverse);
        double subsumptionB = MutantSet.subsumption(matrixB.getKillMap(), matrixA.getKillMap(), disjointUniverse);

        Assertions.assertEquals(0.333333, subsumptionA, 0.00001);
        Assertions.assertEquals(0.5, subsumptionB, 0.00001);

    }

}