package ivansCode.metrics;

import java.lang.reflect.Array;
import java.util.*;

public final class SetCover {

    private SetCover(){}

    public static <K, V> Set<K> greedyBigStep(Map<K, Set<V>> map, int p){

        Set<V> completeSet = new HashSet<>();
        for (Set<V> set : map.values()){
            completeSet.addAll(set);
        }

        Set<V> coveredSet = new HashSet<>();
        Set<K> coveringKeys = new HashSet<>();
        List<K> keyList = new ArrayList<>(map.keySet());

        while (coveredSet.size() != completeSet.size()){

            Set<V> largestSet = new HashSet<>();
            Set<K> largestSetKeys = new HashSet<>();

            for (int numElements = 0; numElements <= p && keyList.size() >= numElements; numElements++){

                int[] keyIndexArr = new int[numElements];
                for (int i = 0; i < keyIndexArr.length; i++){
                    keyIndexArr[i] = i;
                }

                Set<V> currentSet = new HashSet<>();
                Set<K> currentSetKeys = new HashSet<>();

                do {
                    currentSet.clear();
                    currentSetKeys.clear();
                    for (int i : keyIndexArr){
                        currentSet.addAll(map.get(keyList.get(i)));
                        currentSetKeys.add(keyList.get(i));
                    }
                    currentSet.removeAll(coveredSet);
                    if (currentSet.size() > largestSet.size()){
                        largestSet.clear();
                        largestSet.addAll(currentSet);
                        largestSetKeys.clear();
                        largestSetKeys.addAll(currentSetKeys);
                    }
                } while (advanceCombination(keyList.size(), keyIndexArr));

            }

            keyList.removeAll(largestSetKeys);

            coveredSet.addAll(largestSet);
            largestSet.clear();
            coveringKeys.addAll(largestSetKeys);
            largestSetKeys.clear();

        }

        return coveringKeys;

    }

    // arr is strictly ordered and elements are 0-indexed
    private static boolean advanceCombination(int limit, int[] arr){
        for (int i = arr.length - 1; i >= 0; i--){
            if (arr[i] < limit - arr.length + i){
                int startingNum = arr[i];
                for (int j = i; j < arr.length; j++){
                    arr[j] = ++startingNum;
                }
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args){

        Set<Integer> resultKeys = greedyBigStep(
                Map.of(
                        0, Set.of(0, 2),
                        1, Set.of(2, 3),
                        2, Set.of(3, 4),
                        3, Set.of(1, 2, 3, 4),
                        4, Set.of(4)
                ), 2);

        System.out.println(Arrays.toString(resultKeys.toArray()));

        /*
        int[] startingCombination = new int[]{0, 1, 2};
        int limit = 5;

        do {
            System.out.println(Arrays.toString(startingCombination));
        } while (advanceCombination(limit, startingCombination));
         */

    }

}
