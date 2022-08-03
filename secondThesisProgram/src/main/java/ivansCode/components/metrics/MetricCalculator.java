package ivansCode.components.metrics;

import ivansCode.components.Matrix;
import ivansCode.components.metrics.Coupling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricCalculator {

    private final Matrix matrix;

    private final Set<String> minimumTestSet;
    private final Set<String> completeTestSet;
    private final Set<Integer> completeMutantSet;
    private final Set<Integer> disjointMutantSet;
    private final Set<Integer> nonEquivalentMutantSet;
    private final Set<Integer> killedMutants;
    private final Set<Integer> equivalentMutantSet;

    private final Map<String, Set<String>> testGroupMap;

    public MetricCalculator(Matrix matrix, Map<String, Set<String>> testGroupMap){

        this.matrix = matrix;
        this.minimumTestSet = matrix.getMinimumTests();
        this.completeTestSet = matrix.getAllTests();
        this.completeMutantSet = matrix.getAllMutants();
        this.disjointMutantSet = matrix.getDisjointMutants();
        this.nonEquivalentMutantSet = matrix.getNonEquivalentMutants();
        this.killedMutants = matrix.getKilledMutants();
        this.equivalentMutantSet = matrix.getEquivalentMutants();
        this.testGroupMap = testGroupMap;

    }

    public double getVariety(){
        return (double) minimumTestSet.size() / completeTestSet.size();
    }

    public double getEfficiency(){
        return (double) minimumTestSet.size() / (completeTestSet.size() * completeMutantSet.size());
    }

    public Map<Integer, Double> getEasinessValuesOverDisjointSet(){
        Map<Integer, Double> result = new HashMap<>();
        for (Integer currentMutant : disjointMutantSet){
            result.put(currentMutant, ((double) matrix.getKillingTests(currentMutant).size()) / completeTestSet.size());
        }
        return result;
    }

    public Map<Integer, Double> getEasinessValuesOverNonEquivalentSet(){
        Map<Integer, Double> result = new HashMap<>();
        for (Integer currentMutant : nonEquivalentMutantSet){
            result.put(currentMutant, ((double) matrix.getKillingTests(currentMutant).size()) / completeTestSet.size());
        }
        return result;
    }

    public double getInflation(){
        return ((double) completeMutantSet.size() - disjointMutantSet.size()) / completeMutantSet.size();
    }

    public double getMutationScore(){
        return ((double) killedMutants.size()) / completeMutantSet.size();
    }

    public double getEquivalenceRate(){
        return ((double) equivalentMutantSet.size()) / completeMutantSet.size();
    }

    public Map<String, Double> getCouplingValues(){
        Map<String, Double> map = new HashMap<>();
        for (Map.Entry<String, Set<String>> group : testGroupMap.entrySet()){
            double maxValue = 0;
            for (Integer mutant : completeMutantSet){
                Set<String> currentKillingTests = matrix.getKillingTests(mutant);
                if (currentKillingTests.containsAll(group.getValue())){
                    double currentValue = ((double) completeTestSet.size()
                            - Coupling.distance(currentKillingTests, group.getValue()))
                            / completeTestSet.size();
                    if (currentValue > maxValue){ maxValue = currentValue; }
                }
            }
            map.put(group.getKey(), maxValue);
        }
        return map;
    }

}
