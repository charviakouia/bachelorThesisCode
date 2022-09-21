package ivansCode;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import ivansCode.components.Matrix;
import ivansCode.components.ProjectTestSuite;
import ivansCode.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.apache.commons.lang3.StringUtils.indexOfDifference;

public class Main {

    private static final String OUTPUT_FILE_NAME = "equivalentMutants.txt";

    public static void main(String[] args) throws IOException {

        ApplicationProperties.readApplicationProperties();
        List<ProjectTestSuite> projectTestSuites = ApplicationProperties.readProjectTestSuites();
        ThreadService.startup();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        for (ProjectTestSuite projectTestSuite : projectTestSuites){

            Path projectPath = projectTestSuite.getPathToProject();
            Path[] experimentPaths = Files.list(projectPath).filter(s -> !s.getFileName().toString().startsWith("."))
                    .toList().toArray(new Path[0]);

            Map<Set<String>, Boolean> isEquivalent = new HashMap<>();

            for (Path experimentPath : experimentPaths){

                Matrix matrix = MatrixLoader.loadMatrix(experimentPath, projectTestSuite);

                Set<Integer> liveMutantIds = matrix.getAllMutants();
                liveMutantIds.removeAll(matrix.getKilledMutants());

                Set<Integer> equivalentIds = new HashSet<>();
                for (Integer liveId : liveMutantIds){

                    determineEquivalence(experimentPath, equivalentIds, liveId, reader, isEquivalent);

                }

                Path resultFilePath = Paths.get(experimentPath.toString(), OUTPUT_FILE_NAME);
                PrintWriter writer = new PrintWriter(Files.newOutputStream(resultFilePath, CREATE, APPEND));
                String result = equivalentIds.stream().sorted().map(Object::toString).collect(Collectors.joining(","));
                writer.print(result);
                writer.close();

                System.out.println("Experiment finished" + System.lineSeparator());
                reader.readLine();

            }

        }

        reader.close();
        ThreadService.shutdown();

    }

    private static void determineEquivalence(Path experimentPath, Set<Integer> equivalentIds, Integer liveId,
                                             BufferedReader reader, Map<Set<String>, Boolean> isEquivalent)
            throws IOException {

        String originalCode = CodeLoader.getOriginalSourceCode(experimentPath);
        String mutantCode = CodeLoader.getSourceCodeForMutant(experimentPath, liveId);
        String formattedOriginal;
        String formattedMutant;

        Set<String> testSet = new HashSet<>();
        try {
            Formatter formatter = new Formatter();
            formattedOriginal = formatter.formatSource(originalCode);
            formattedMutant = formatter.formatSource(mutantCode);
            testSet.add(formattedOriginal);
            testSet.add(formattedMutant);
        } catch (FormatterException e){
            return;
        }

        if (formattedOriginal.equals(formattedMutant)){
            equivalentIds.add(liveId);
            return;
        } else if (isEquivalent.containsKey(testSet)){
            if (isEquivalent.get(testSet)){
                equivalentIds.add(liveId);
            }
            return;
        }

        String input = null;

        do {

            try {

                String differenceMessage = getDifferenceMessage(formattedOriginal, formattedMutant);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CODE START");
                System.out.println(differenceMessage);
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CODE END");
                System.out.println();
                System.out.print("Are these equivalent? (y/n) >> ");

                input = reader.readLine();

                System.out.println(System.lineSeparator().repeat(15));

            } catch (IllegalArgumentException e){
                return;
            }

        } while (!input.equals("y") && !input.equals("n"));

        if (input.equals("y")){
            equivalentIds.add(liveId);
        }

        isEquivalent.put(testSet, input.equals("y"));

    }

    private static String getDifferenceMessage(String original, String mutated){

        int firstDiffIndex = indexOfDifference(original, mutated);
        int firstDiffIndexFromRear = indexOfDifference(
                new StringBuilder(original).reverse().toString(),
                new StringBuilder(mutated).reverse().toString());

        if (firstDiffIndex > original.length() - firstDiffIndexFromRear){
            firstDiffIndexFromRear = original.length() - firstDiffIndex;
        } else if (firstDiffIndex > mutated.length() - firstDiffIndexFromRear){
            firstDiffIndexFromRear = mutated.length() - firstDiffIndex;
        }

        int lastDiffIndexInOriginal = original.length() - firstDiffIndexFromRear;
        String originalSubstring = original.substring(firstDiffIndex, lastDiffIndexInOriginal);

        int lastDiffIndexInMutated = mutated.length() - firstDiffIndexFromRear;
        String mutatedSubstring = mutated.substring(firstDiffIndex, lastDiffIndexInMutated);

        return String.format(
                "%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",
                original.substring(0, firstDiffIndex),
                System.lineSeparator(),
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ORIGINAL START",
                System.lineSeparator(),
                originalSubstring,
                System.lineSeparator(),
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ORIGINAL END",
                System.lineSeparator(),
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> MUTATED START",
                System.lineSeparator(),
                mutatedSubstring,
                System.lineSeparator(),
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> MUTATED END",
                System.lineSeparator(),
                original.substring(lastDiffIndexInOriginal)
        );

    }

}
