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

    public static byte[] compileTo(Path destinationPath, String name, String sourceCode, boolean deleteIfExists)
            throws IOException {
        Files.createDirectories(destinationPath);
        File file = Paths.get(destinationPath.toString(), name + ".java").toFile();
        if (!Files.exists(file.toPath())){
            Files.createFile(file.toPath());
        } else if (deleteIfExists){
            Files.delete(file.toPath());
            Files.createFile(file.toPath());
        } else {
            return null;
        }
        Files.writeString(file.toPath(), sourceCode);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler.run(null, null, null, file.getPath()) != 0){
            return null;
        } else {
            return Files.readAllBytes(Paths.get(destinationPath.toString(), name + ".class"));
        }
    }

    public static void saveTo(Path destinationPath, String name, String type, byte[] content, boolean deleteIfExists)
            throws IOException {
        Files.createDirectories(destinationPath);
        File file = Paths.get(destinationPath.toString(), name + type).toFile();
        if (!Files.exists(file.toPath())){
            Files.createFile(file.toPath());
        } else if (deleteIfExists){
            Files.delete(file.toPath());
            Files.createFile(file.toPath());
        } else {
            return;
        }
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

    /*
    public static void main(String[] args) throws IOException {

        ApplicationProperties.readApplicationProperties();
        clearDirectory(ApplicationProperties.getTempPath());

    }
     */

}
