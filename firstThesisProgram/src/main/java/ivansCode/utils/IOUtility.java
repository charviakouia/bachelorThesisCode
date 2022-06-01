package ivansCode.utils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class IOUtility {

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
        compiler.run(null, null, null, file.getPath());
        return Files.readAllBytes(Paths.get(destinationPath.toString(), name + ".class"));
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

}
