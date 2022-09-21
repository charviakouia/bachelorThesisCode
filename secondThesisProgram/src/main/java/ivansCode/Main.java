package ivansCode;

import ivansCode.components.Matrix;
import ivansCode.components.ProjectTestSuite;
import ivansCode.components.metrics.Coupling;
import ivansCode.components.metrics.MetricCalculator;
import ivansCode.components.metrics.MutantSet;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.BasicMath;
import ivansCode.utils.MatrixLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main {

    private static final String OUTPUT_FILE_NAME = "result.txt";

    public static void main(String[] args) throws IOException {

        ApplicationProperties.readApplicationProperties();
        List<ProjectTestSuite> projectTestSuites = ApplicationProperties.readProjectTestSuites();

        for (ProjectTestSuite projectTestSuite : projectTestSuites){

            Map<String, Set<String>> testGroupMap = projectTestSuite.getTestGroupMap();
            Path projectPath = projectTestSuite.getPathToProject();

            Path[] experimentPaths = Files.list(projectPath).filter(s -> !s.getFileName().toString().startsWith("."))
                    .toList().toArray(new Path[0]);

            Map<Integer, Set<String>> mutantUniverseMap = new HashMap<>();
            for (Path experimentPath : experimentPaths){
                Map<Integer, Set<String>> killMap = MatrixLoader.getKillMap(experimentPath, projectTestSuite);
                mutantUniverseMap.putAll(killMap);
            }
            Set<Set<String>> disjointUniverse = MutantSet.disjointMutantSet(mutantUniverseMap).stream()
                    .map(mutantUniverseMap::get).collect(Collectors.toSet());

            for (Path experimentPath : experimentPaths){

                Path resultFilePath = Paths.get(experimentPath.toString(), OUTPUT_FILE_NAME);
                PrintWriter writer = new PrintWriter(Files.newOutputStream(resultFilePath, CREATE, APPEND));

                Matrix matrix = MatrixLoader.loadMatrix(experimentPath, projectTestSuite);
                MetricCalculator metrics = new MetricCalculator(matrix, testGroupMap, disjointUniverse);

                writer.printf("Variety metric: %.5f%s", metrics.getVariety(), System.lineSeparator());

                writer.printf("Efficiency metric: %.5f%s", metrics.getEfficiency(), System.lineSeparator());

                Map<Set<String>, Double> easinessOverDisjointMap =
                        metrics.getEasinessValuesOverUniversallyDisjointSet();
                writer.printf("Average easiness metric over disjoint mutant set: %.5f%s",
                        BasicMath.getArithmeticAverage(new LinkedList<>(easinessOverDisjointMap.values())),
                        System.lineSeparator());
                writer.printf("Easiness metric for each over disjoint mutant set: %s%s",
                        easinessOverDisjointMap.values().stream().sorted().map(String::valueOf)
                                .collect(Collectors.joining(", ")),
                        System.lineSeparator());

                Map<Integer, Double> easinessOverKilledSet = metrics.getEasinessValuesOverKilledSet();
                writer.printf("Easiness metric for each killed mutant: %s%s",
                        easinessOverKilledSet.values().stream().sorted().map(String::valueOf)
                                .collect(Collectors.joining(", ")),
                        System.lineSeparator());

                writer.printf("Number of generated mutants: %d%s", metrics.getNumMutants(), System.lineSeparator());

                writer.printf("Inflation metric: %.5f%s", metrics.getInflation(),
                        System.lineSeparator());

                writer.printf("Mutation score metric: %.5f%s", metrics.getMutationScore(), System.lineSeparator());

                writer.printf("Equivalence rate metric: %.5f%s", metrics.getEquivalenceRate(), System.lineSeparator());

                Map<String, Double> couplingValuesMap = metrics.getCouplingValues();
                writer.printf("Average coupling metric: %.5f%s",
                        BasicMath.getArithmeticAverage(new LinkedList<>(couplingValuesMap.values())),
                        System.lineSeparator());
                writer.printf("Coupling metric for each over all groups: %s, %s",
                        couplingValuesMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .map(entry -> String.format("(%s, %s)", entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining(", ")),
                        System.lineSeparator());

                writer.close();

            }

            for (int i = 0; i < experimentPaths.length; i++){

                for (int j = 0; j < experimentPaths.length; j++){

                    if (i == j){ continue; }

                    Map<Integer, Set<String>> fstKillMap =
                            MatrixLoader.getKillMap(experimentPaths[i], projectTestSuite);
                    Map<Integer, Set<String>> sndKillMap =
                            MatrixLoader.getKillMap(experimentPaths[j], projectTestSuite);

                    Path fstOutputPath = Paths.get(experimentPaths[i].toString(), OUTPUT_FILE_NAME);
                    PrintWriter writer = new PrintWriter(Files.newOutputStream(fstOutputPath, CREATE, APPEND));

                    writer.printf("Subsumption metric of %s with respect to %s: %.5f%s",
                            experimentPaths[i], experimentPaths[j],
                            MutantSet.subsumption(fstKillMap, sndKillMap, disjointUniverse),
                            System.lineSeparator()
                    );
                    writer.close();

                }

            }

        }

    }

}
