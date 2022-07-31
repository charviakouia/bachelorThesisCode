package ivansCode;

import ivansCode.components.Matrix;
import ivansCode.components.ProjectTestSuite;
import ivansCode.metrics.SetCover;
import utils.ApplicationProperties;
import utils.MatrixLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main {

    private static final String OUTPUT_FILE_NAME = "result.txt";

    public static void main(String[] args) throws IOException {

        ApplicationProperties.readApplicationProperties();

        List<ProjectTestSuite> projectTestSuites = ApplicationProperties.readProjectTestSuites();
        for (ProjectTestSuite projectTestSuite : projectTestSuites){
            Path projectPath = projectTestSuite.getPathToProject();
            Path[] experimentPaths = Files.list(projectPath).filter(s -> !s.getFileName().toString().startsWith("."))
                    .toList().toArray(new Path[0]);
            for (Path experimentPath : experimentPaths){
                Path resultFilePath = Paths.get(experimentPath.toString(), OUTPUT_FILE_NAME);
                PrintWriter writer = new PrintWriter(Files.newOutputStream(resultFilePath, CREATE, APPEND));

                Matrix matrix = MatrixLoader.loadMatrix(experimentPath, projectTestSuite);
                Set<String> minimumTestSet = matrix.minimumTestSet();
                Set<String> completeTestSet = matrix.allTests();
                writer.printf("Variety metric: %.5f", (double) minimumTestSet.size() / completeTestSet.size());

                // Calculate metrics for each experiment and save them to the same location

                writer.close();
            }
            for (int i = 0; i < experimentPaths.length; i++){
                for (int j = 0; j < experimentPaths.length; j++){
                    if (i == j){ continue; }
                    Path experimentPath =
                            Paths.get(experimentPaths[i].toString(), OUTPUT_FILE_NAME);
                    PrintWriter writer = new PrintWriter(Files.newOutputStream(experimentPath, CREATE, APPEND));

                    // writer.printf("Test output - %d to %d%s", i, j, System.lineSeparator());
                    // Calculate pairwise metrics and save them to the first experiment's location

                    writer.close();
                }
            }
        }

    }

}
