package ivansCode.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.google.common.io.Files;
import ivansCode.components.Mutant;
import ivansCode.components.techniques.Technique;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExampleTechnique implements Technique {

    private final Class<?> originalClass;
    private final CompilationUnit compilationUnit;
    private final Path temporaryPath;
    private int numRuns = 0;

    public ExampleTechnique(Class<?> originalClass, Path sourceCodePath, Path temporaryPath) throws Exception {

        this.originalClass = originalClass;
        this.temporaryPath = temporaryPath;

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
        return numRuns <= 1;
    }

    @Override
    public Mutant next() {
        if (numRuns == 0){
            try {
                String simpleName = originalClass.getSimpleName();

                File originalFile = Paths.get(temporaryPath.toString(), simpleName + ".java").toFile();
                Files.touch(originalFile);
                Files.write(compilationUnit.toString().getBytes(StandardCharsets.UTF_8), originalFile);

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                compiler.run(null, null, null, originalFile.getPath());
                byte[] originalBytes =
                        java.nio.file.Files.readAllBytes(Path.of(temporaryPath + "/" + simpleName + ".class"));
                return new Mutant(originalClass, originalBytes, compilationUnit.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                numRuns++;
            }
        } else {
            try {
                CompilationUnit clone = compilationUnit.clone();
                clone.findAll(BinaryExpr.class).stream().filter(c -> c.getOperator().equals(BinaryExpr.Operator.PLUS))
                        .forEach(c -> c.setOperator(BinaryExpr.Operator.MINUS));

                String simpleName = originalClass.getSimpleName();

                File mutantFile = Paths.get(temporaryPath.toString(), simpleName + ".java").toFile();
                Files.touch(mutantFile);
                Files.write(clone.toString().getBytes(StandardCharsets.UTF_8), mutantFile);

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                compiler.run(null, null, null, mutantFile.getPath());
                byte[] mutatedBytes =
                        java.nio.file.Files.readAllBytes(Path.of(temporaryPath + "/" + simpleName + ".class"));

                return new Mutant(originalClass, mutatedBytes, clone.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                numRuns++;
            }
        }
    }

    @Override
    public String getDescription(){
        return "exampleTechnique";
    }

}
