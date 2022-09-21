package ivansCode.techniques.PITTechnique.utils;

import ivansCode.utils.ApplicationProperties;
import joptsimple.internal.Strings;
import org.pitest.mutationtest.commandline.MutationCoverageReport;
import org.pitest.plugin.export.MutantExportFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PITExecutor {

    public enum ExecutionSetting {

        DEFAULTS("DEFAULTS"),
        STRONGER("STRONGER"),
        ALL("ALL");

        private final String commandLineSetting;
        ExecutionSetting(String commandLineSetting){
            this.commandLineSetting = commandLineSetting;
        }

        public String getCommandLineSetting() {
            return commandLineSetting;
        }

    }

    public static List<byte[]> getByteCode(Path projectPath, Path destinationPath, String className,
                                           Path targetCodePath, String[] testPaths, ExecutionSetting executionSetting)
            throws IOException {

        String projectName = projectPath.getFileName().toString();
        Path pathToByteCode = projectPath.getParent().resolve(projectName + "PIT");
        pathToByteCode = pathToByteCode.resolve(executionSetting.getCommandLineSetting());

        for (String pathElement : className.split("\\.")){
            pathToByteCode = pathToByteCode.resolve(pathElement);
        }
        pathToByteCode = pathToByteCode.resolve("mutants");
        int numMutants = Files.list(pathToByteCode).toList().size();

        List<byte[]> bytes = new LinkedList<>();
        for (int i = 0; i < numMutants; i++){
            Path currentByteCode = pathToByteCode.resolve(String.valueOf(i))
                    .resolve(className + ".class");
            bytes.add(Files.readAllBytes(currentByteCode));
        }

        return bytes;

    }

}
