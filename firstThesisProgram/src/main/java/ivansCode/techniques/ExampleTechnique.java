package ivansCode.techniques;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.google.common.io.Files;
import ivansCode.agent.CustomAgent;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class ExampleTechnique implements Technique {

    private final Class<?> originalClass;
    private final CompilationUnit compilationUnit;
    private boolean wasRun = false;
    private final Path sourceCodePath;

    public ExampleTechnique(Class<?> originalClass, Path sourceCodePath) throws Exception {

        this.originalClass = originalClass;
        this.sourceCodePath = sourceCodePath;

        String javaCode = String.join("", Files.readLines(sourceCodePath.toFile(), StandardCharsets.UTF_8));

        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = javaParser.parse(javaCode);
        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()){
            this.compilationUnit = parseResult.getResult().get();
        } else {
            throw new IllegalArgumentException("Couldn't parse class");
        }

    }

    @Override
    public boolean hasNext() {
        return !wasRun;
    }

    @Override
    public Class<?> next() {

        wasRun = true;
        CompilationUnit clone = compilationUnit.clone();
        clone.findAll(BinaryExpr.class).stream().filter(c -> c.getOperator().equals(BinaryExpr.Operator.PLUS))
                        .forEach(c -> c.setOperator(BinaryExpr.Operator.MINUS));
        try {
            String name = originalClass.getSimpleName();
            File sourceCode = sourceCodePath.toFile();
            Files.touch(sourceCode);
            Files.write(clone.toString().getBytes(StandardCharsets.UTF_8), sourceCode);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, sourceCode.getPath());
            CustomAgent.introduceMutation(originalClass,
                    java.nio.file.Files.readAllBytes(
                            Path.of(sourceCodePath.getParent().toFile().getAbsolutePath() + "/" + name + ".class")));
            return originalClass;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}
