package ivansCode.components;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Matrix {

    private boolean[][] matrix;
    private final Map<String, Integer> testNameToIndex = new HashMap<>();
    private final Map<Integer, Integer> mutantIdToIndex = new HashMap<>();
    private final Set<Integer> eqMutantSet;

    public Matrix(Set<Pair<String, Integer>> killRelation, Set<String> testNames, Set<Integer> mutantIds,
                  Set<Integer> equivalentMutants){

        int currentTestNameIndex = 0;
        for (String testName : testNames){
            testNameToIndex.put(testName, currentTestNameIndex++);
        }

        int currentMutantId = 0;
        for (Integer mutantId : mutantIds){
            mutantIdToIndex.put(mutantId, currentMutantId++);
        }

        for (Pair<String, Integer> pair : killRelation){
            if (!testNames.contains(pair.getLeft())){
                throw new IllegalArgumentException("Test name: " + pair.getLeft() + " not recognized");
            }
            if (!mutantIds.contains(pair.getRight())){
                throw new IllegalArgumentException("Mutant id: " + pair.getRight() + " not recognized");
            }
        }

        for (Integer mutantId : equivalentMutants){
            if (!mutantIds.contains(mutantId)){
                throw new IllegalArgumentException("Mutant id: " + mutantId + "not recognized");
            }
        }
        eqMutantSet = new HashSet<>(equivalentMutants);

        initializeKillMatrix(killRelation, currentTestNameIndex, currentMutantId);

    }

    private void initializeKillMatrix(Set<Pair<String, Integer>> killRelation, int numRows, int numColumns){

        matrix = new boolean[numRows][numColumns];
        for (int i = 0; i < numRows; i++){
            for (int j = 0; j < numColumns; j++){
                matrix[i][j] = false;
            }
        }
        for (Pair<String, Integer> pair : killRelation){
            matrix[testNameToIndex.get(pair.getLeft())][mutantIdToIndex.get(pair.getRight())] = true;
        }

    }

    public boolean wasKilled(String testName, int mutantId){
        return matrix[testNameToIndex.get(testName)][mutantIdToIndex.get(mutantId)];
    }

    public Set<String> getAllTests(){
        return new HashSet<>(testNameToIndex.keySet());
    }

    public Set<Integer> getAllMutants(){
        return new HashSet<>(mutantIdToIndex.keySet());
    }

    public Set<String> getKillingTests(int mutantId){
        Set<String> result = new HashSet<>();
        for (String testName : testNameToIndex.keySet()){
            if (wasKilled(testName, mutantId)){
                result.add(testName);
            }
        }
        return result;
    }

    public Set<Integer> getNonEquivalentMutants(){
        Set<Integer> result = getAllMutants();
        result.removeAll(eqMutantSet);
        return result;
    }

    public Set<Integer> getKilledMutants(){
        Set<Integer> result = new HashSet<>();
        for (Integer currentMutant : mutantIdToIndex.keySet()){
            boolean wasKilled = false;
            for (String currentTest : testNameToIndex.keySet()){
                if (wasKilled(currentTest, currentMutant)){
                    wasKilled = true;
                    break;
                }
            }
            if (wasKilled){
                result.add(currentMutant);
            }
        }
        return result;
    }

    public Set<Integer> getEquivalentMutants(){
        return new HashSet<>(eqMutantSet);
    }

    public Map<Integer, Set<String>> getKillMap(){
        Map<Integer, Set<String>> killMap = new HashMap<>();
        for (Integer currentMutant : mutantIdToIndex.keySet()){
            Set<String> currentTestSet = new HashSet<>();
            for (String currentTestName : testNameToIndex.keySet()){
                if (wasKilled(currentTestName, currentMutant)){
                    currentTestSet.add(currentTestName);
                }
            }
            killMap.put(currentMutant, currentTestSet);
        }
        return killMap;
    }

    public Map<String, Set<Integer>> getTestCoverMap(){
        Map<String, Set<Integer>> testNameToMutantSet = new HashMap<>();
        for (String testName : testNameToIndex.keySet()){
            Set<Integer> currentMutantSet = new HashSet<>();
            for (Integer currentMutant : mutantIdToIndex.keySet()){
                if (wasKilled(testName, currentMutant)){
                    currentMutantSet.add(currentMutant);
                }
            }
            testNameToMutantSet.put(testName, currentMutantSet);
        }
        return testNameToMutantSet;
    }

}
