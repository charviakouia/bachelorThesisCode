package ivansCode.components;

import com.github.javaparser.StaticJavaParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Project {

    private final String subjectClassName;
    private final String simpleSubjectClassName;
    private final Path sourceCodePath;
    private final Path targetCodePath;
    private final Path projectPath;
    private final Set<String> testMethodNames;
    private final String originalSourceCode;

    public Project(String subjectClassName, String simpleSubjectClassName, Path sourceCodePath, Path targetCodePath,
                   Path projectPath, Set<String> testMethodNames) throws IOException {

        this.subjectClassName = subjectClassName;
        this.simpleSubjectClassName = simpleSubjectClassName;
        this.sourceCodePath = sourceCodePath;
        this.targetCodePath = targetCodePath;
        this.projectPath = projectPath;
        this.testMethodNames = new HashSet<>(testMethodNames);
        this.originalSourceCode = StaticJavaParser.parse(sourceCodePath.toFile()).toString();

    }

    public String getOriginalSourceCode() {
        return originalSourceCode;
    }

    public Path getTargetCodePath() {
        return targetCodePath;
    }

    public String getSubjectClassName() {
        return subjectClassName;
    }

    public String getSimpleSubjectClassName() {
        return simpleSubjectClassName;
    }

    public Path getSourceCodePath() {
        return sourceCodePath;
    }

    public Set<String> getTestMethodNames() {
        return new HashSet<>(testMethodNames);
    }

    public Path getProjectPath() {
        return projectPath;
    }

}
