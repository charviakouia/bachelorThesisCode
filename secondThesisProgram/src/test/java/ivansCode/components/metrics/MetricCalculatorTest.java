package ivansCode.components.metrics;

import ivansCode.components.Matrix;
import ivansCode.utils.BasicMath;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetricCalculatorTest {

    private static Matrix matrix;
    private static Map<String, Set<String>> testGroupMap;

    @BeforeAll
    static void init(){

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

        testGroupMap = Map.of(
                "G1", Set.of("A", "B"),
                "G2", Set.of("D")
        );

    }

    @Test
    void testVariety(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, new HashSet<>());
        Assertions.assertEquals(0.4, calculator.getVariety(), 0.00001);

    }

    @Test
    void testEfficiency(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, new HashSet<>());
        Assertions.assertEquals(0.0666666666, calculator.getEfficiency(), 0.00001);

    }

    @Test
    void testEasiness(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, Set.of(Set.of("C")));
        double avg = BasicMath.getArithmeticAverage(
                new LinkedList<>(calculator.getEasinessValuesOverUniversallyDisjointSet().values()));
        Assertions.assertEquals(0.2, avg, 0.00001);

        MetricCalculator calculatorA = new MetricCalculator(matrix, testGroupMap,
                Set.of(Set.of("C"), Set.of("A", "B")));
        double avgA = BasicMath.getArithmeticAverage(
                new LinkedList<>(calculatorA.getEasinessValuesOverUniversallyDisjointSet().values()));
        Assertions.assertEquals(0.3, avgA, 0.00001);

    }

    @Test
    void testInflation(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap,
                Set.of(Set.of("A", "B"), Set.of("C")));
        Assertions.assertEquals(2, calculator.getInflation(), 0.00001);

        MetricCalculator calculatorA = new MetricCalculator(matrix, testGroupMap,
                Set.of(Set.of("C")));
        Assertions.assertEquals(5, calculatorA.getInflation(), 0.00001);

    }

    @Test
    void testMutationScore(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, new HashSet<>());
        Assertions.assertEquals(0.6666666666, calculator.getMutationScore(), 0.00001);

    }

    @Test
    void testEquivalenceRate(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, new HashSet<>());
        Assertions.assertEquals(0.3333333333, calculator.getEquivalenceRate(), 0.00001);

    }

    @Test
    void testCoupling(){

        MetricCalculator calculator = new MetricCalculator(matrix, testGroupMap, new HashSet<>());
        double avg = BasicMath.getArithmeticAverage(
                new LinkedList<>(calculator.getCouplingValues().values()));
        Assertions.assertEquals(0.8, avg, 0.00001);

    }

}