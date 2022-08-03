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

    public static List<byte[]> getByteCode(Path destinationPath, Class<?> targetClass, Path sourceCodePath,
                                           String[] testPaths) throws IOException {

        String[] array = new String[]{
                "--reportDir", destinationPath.toString(),
                "--targetClasses", targetClass.getCanonicalName(),
                "--targetTests", Strings.join(testPaths, ";"),
                "--sourceDirs", sourceCodePath.toString(),
                "--outputFormats", "XML",
                "--features", "+export"
        };

        MutationCoverageReport.main(array);

        Path pathToByteCode = destinationPath.resolve("export");
        for (String pathElement : targetClass.getCanonicalName().split("\\.")){
            pathToByteCode = pathToByteCode.resolve(pathElement);
        }
        pathToByteCode = pathToByteCode.resolve("mutants");
        int numMutants = Files.list(pathToByteCode).toList().size();

        List<byte[]> bytes = new LinkedList<>();
        for (int i = 0; i < numMutants; i++){
            Path currentByteCode = pathToByteCode.resolve(String.valueOf(i))
                    .resolve(targetClass.getCanonicalName() + ".class");
            bytes.add(Files.readAllBytes(currentByteCode));
        }

        return bytes;

    }

}
