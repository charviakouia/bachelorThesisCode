package ivansCode.utils;

import ivansCode.components.Matrix;
import ivansCode.components.ProjectTestSuite;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class MatrixLoader {

    private MatrixLoader(){}

    public static Matrix loadMatrix(Path outputPath, ProjectTestSuite projectTestSuite) throws IOException {

        List<String> matrixDataList = Files.readAllLines(outputPath.resolve("matrix.txt"));
        Set<String> testSet = projectTestSuite.getTests();
        Set<Pair<String, Integer>> killRelation = getKillRelation(matrixDataList, testSet);
        Set<Integer> mutantIdSet = Arrays.stream(matrixDataList.get(0).replaceAll("\\s+", "")
                .split("\\|")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).boxed()
                .collect(Collectors.toSet());

        List<String> equivalentMutantsDataList = Files.readAllLines(outputPath.resolve("equivalentMutants.txt"));
        String equivalentMutantsData = equivalentMutantsDataList.isEmpty() ? "" : equivalentMutantsDataList.get(0);
        Set<Integer> equivalentMutantsSet = Arrays.stream(equivalentMutantsData.split("\\s*,\\s*"))
                .filter(s -> !s.isEmpty()).mapToInt(s -> Integer.parseInt(s.trim())).boxed()
                .collect(Collectors.toSet());

        return new Matrix(killRelation, testSet, mutantIdSet, equivalentMutantsSet);

    }

    private static Set<Pair<String, Integer>> getKillRelation(List<String> dataList, Set<String> tests){

        Iterator<String> iterator = dataList.iterator();

        String headerString = iterator.next();
        int cellSize = headerString.split("\\|")[0].length();
        int[] headerMutantIds = Arrays.stream(headerString.replaceAll("\\s+", "")
                .split("\\|")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();

        Map<String, String> suffixToTest = new HashMap<>();
        for (String test : tests){
            String currentSuffix = test.substring(Math.max(0, test.length() - cellSize));
            if (suffixToTest.containsKey(currentSuffix)){
                throw new IllegalArgumentException("Suffix '" + currentSuffix + "' not unique");
            }
            assert !suffixToTest.containsKey(currentSuffix);
            suffixToTest.put(currentSuffix, test);
        }

        Set<Pair<String, Integer>> killRelation = new HashSet<>();
        while (iterator.hasNext()){
            String entry = iterator.next();
            if (StringUtils.containsOnly(entry, '-', 0)){
                continue;
            } else {
                String testNameSuffix = entry.substring(0, cellSize).trim();
                Boolean[] killRow = Arrays.stream(StringUtils.normalizeWhitespace(entry.substring(cellSize))
                        .split("\\|")).filter(s -> !s.isEmpty())
                        .map(s -> StringUtils.containsOnly(s, 'X', 1)).toList().toArray(new Boolean[0]);
                if (killRow.length != headerMutantIds.length){
                    throw new IllegalStateException("Incorrect data format - mismatch in the number of columns");
                }
                String testName = suffixToTest.get(testNameSuffix);
                if (testName == null){
                    throw new IllegalStateException("Test name: " + testNameSuffix + " not registered");
                }
                for (int i = 0; i < headerMutantIds.length; i++){
                    if (killRow[i]){
                        killRelation.add(Pair.of(testName, headerMutantIds[i]));
                    }
                }
            }
        }

        return killRelation;

    }

    public static Map<Integer, Set<String>> getKillMap(Path experimentPath, ProjectTestSuite projectTestSuite)
            throws IOException {

        Matrix fstMatrix = MatrixLoader.loadMatrix(experimentPath, projectTestSuite);
        return fstMatrix.getKillMap();

    }

}
