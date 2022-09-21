package ivansCode.utils;

import org.apache.commons.io.FileUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class IOUtility {

    private IOUtility(){}

    public static byte[] compileTo(Path destinationPath, String name, String sourceCode)
            throws IOException {

        Files.createDirectories(destinationPath);

        File javaFile = Paths.get(destinationPath.toString(), name + ".java").toFile();
        if (Files.exists(javaFile.toPath())){
            Files.delete(javaFile.toPath());
        }
        Files.createFile(javaFile.toPath());
        Files.writeString(javaFile.toPath(), sourceCode);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler.run(null, null, null, javaFile.getPath()) != 0){
            return null;
        } else if (!Files.exists(Paths.get(destinationPath.toString(), name + ".class"))){
            return null;
        } else {
            return Files.readAllBytes(Paths.get(destinationPath.toString(), name + ".class"));
        }

    }

    public static void saveTo(Path destinationPath, String name, String type, byte[] content) throws IOException {

        Files.createDirectories(destinationPath);

        File file = Paths.get(destinationPath.toString(), name + type).toFile();
        if (Files.exists(file.toPath())){
            Files.delete(file.toPath());
        }
        Files.createFile(file.toPath());
        Files.write(file.toPath(), content);

    }

    public static void clearDirectory(Path path) throws IOException {

        Files.list(path).forEach(filePath -> {
            try {
                if (Files.isDirectory(filePath)){
                    FileUtils.deleteDirectory(filePath.toFile());
                } else {
                    Files.delete(filePath);
                }
            } catch (IOException e){
                throw new IllegalStateException("Couldn't clear the directory");
            }
        });

    }

}
