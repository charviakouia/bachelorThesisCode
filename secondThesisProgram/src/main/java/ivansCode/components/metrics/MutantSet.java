package ivansCode.components.metrics;

import java.util.*;

public final class MutantSet {

    private MutantSet(){}

    public static Set<Integer> disjointMutantSet(Map<Integer, Set<String>> killMap){

        List<Integer> keyList = new ArrayList<>(killMap.keySet());

        Set<Integer> toRemove = new HashSet<>();
        for (Integer mutantId : keyList){
            if (killMap.get(mutantId).isEmpty()){
                toRemove.add(mutantId);
            }
        }
        keyList.removeAll(toRemove);
        toRemove.clear();

        for (int i = 0; i < keyList.size(); i++){
            for (int j = i + 1; j < keyList.size(); j++){
                Set<String> testSet1 = killMap.get(keyList.get(i));
                Set<String> testSet2 = killMap.get(keyList.get(j));
                if (!toRemove.contains(keyList.get(i)) && !toRemove.contains(keyList.get(j))
                        && testSet1.equals(testSet2)){
                    toRemove.add(keyList.get(i));
                }
            }
        }
        keyList.removeAll(toRemove);
        toRemove.clear();

        Set<Integer> result = new HashSet<>();
        while (!keyList.isEmpty()){
            Set<Integer> mostSubsumedMutants = new HashSet<>();
            Integer mostSubsumingMutant = null;
            for (Integer currentSubsumingMutant : keyList){
                Set<Integer> currentSubsumedMutants = new HashSet<>();
                Set<String> currentSubsumingTests = killMap.get(currentSubsumingMutant);
                for (Integer currentSubsumedMutant : keyList){
                    Set<String> currentSubsumedTests = killMap.get(currentSubsumedMutant);
                    if (currentSubsumedTests.containsAll(currentSubsumingTests)){
                        currentSubsumedMutants.add(currentSubsumedMutant);
                    }
                }
                if (currentSubsumedMutants.size() > mostSubsumedMutants.size()){
                    mostSubsumedMutants.clear();
                    mostSubsumedMutants.addAll(currentSubsumedMutants);
                    mostSubsumingMutant = currentSubsumingMutant;
                }
            }
            result.add(mostSubsumingMutant);
            keyList.removeAll(mostSubsumedMutants);
        }

        return result;

    }

    public static double subsumption(Map<Integer, Set<String>> mapA, Map<Integer, Set<String>> mapB){

        Set<Integer> djmA = disjointMutantSet(mapA);
        Set<Set<String>> djmATests = new HashSet<>();
        for (Integer mutant : djmA){
            djmATests.add(mapA.get(mutant));
        }
        Set<Integer> djmB = disjointMutantSet(mapB);

        Map<Integer, Set<String>> djmAUnionDjmBMap = new HashMap<>();
        for (Integer mutant : djmA){
            djmAUnionDjmBMap.put(mutant, mapA.get(mutant));
        }
        for (Integer mutant : djmB){
            djmAUnionDjmBMap.put(mutant, mapB.get(mutant));
        }

        Set<Integer> djmOfDjmAUnionDjmB = disjointMutantSet(djmAUnionDjmBMap);
        Set<Set<String>> djmOfDjmAUnionDjmBTests = new HashSet<>();
        for (Integer mutant : djmOfDjmAUnionDjmB){
            djmOfDjmAUnionDjmBTests.add(djmAUnionDjmBMap.get(mutant));
        }

        Set<Set<String>> djmOfDjmAUnionDjmBMinusDjmATests = new HashSet<>(djmOfDjmAUnionDjmBTests);
        djmOfDjmAUnionDjmBMinusDjmATests.removeAll(djmATests);

        return 1 - ((double) djmOfDjmAUnionDjmBMinusDjmATests.size()) / djmB.size();

    }

}
