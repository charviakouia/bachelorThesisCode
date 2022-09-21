package ivansCode.techniques.PITTechnique.utils;

import com.github.javaparser.StaticJavaParser;
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

    public static String getDecompiledCode(byte[] byteCode, String className, Path destinationPath) throws IOException {

        Path pathToByteCode = destinationPath.resolve(className + ".class");
        Files.createFile(pathToByteCode);
        Files.write(pathToByteCode, byteCode);

        String[] arr = new String[]{
                pathToByteCode.toString(),
                destinationPath.toString()
        };

        ConsoleDecompiler.main(arr);

        Path pathToJavaCode = destinationPath.resolve(className + ".java");

        return StaticJavaParser.parse(pathToJavaCode.toFile()).toString();

    }

}
