package ivansCode.techniques.PITTechnique.utils;

import ivansCode.example.src.A;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.StringUtils;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FernFlowerExecutor {

    public static String getDecompiledCode(byte[] byteCode, Class<?> clazz, Path destinationPath) throws IOException {

        Path pathToByteCode = destinationPath.resolve(clazz.getCanonicalName() + ".class");
        Files.createFile(pathToByteCode);
        Files.write(pathToByteCode, byteCode);

        String[] arr = new String[]{
                pathToByteCode.toString(),
                destinationPath.toString()
        };

        ConsoleDecompiler.main(arr);

        Path pathToJavaCode = destinationPath.resolve(clazz.getCanonicalName() + ".java");
        List<String> lines = Files.readAllLines(pathToJavaCode);

        return StringUtils.normalizeWhitespace(String.join("", lines));

    }

}
