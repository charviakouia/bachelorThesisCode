package ivansCode.components;

import com.google.common.io.Files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Project {

    private String subjectClassName;
    private Path sourceCodePath;
    private String[] testClassNames;

    public Project(String subjectClassName, Path sourceCodePath, String[] testClassNames) {
        this.subjectClassName = subjectClassName;
        this.sourceCodePath = sourceCodePath;
        this.testClassNames = testClassNames;
    }

    public String getOriginalSourceCode() throws IOException {
        return String.join("", Files.readLines(sourceCodePath.toFile(), StandardCharsets.UTF_8));
    }

    public String getSubjectClassName() {
        return subjectClassName;
    }

    public void setSubjectClassName(String subjectClassName) {
        this.subjectClassName = subjectClassName;
    }

    public Path getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodePath(Path sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
    }

    public String[] getTestClassNames() {
        return testClassNames;
    }

    public void setTestClassNames(String[] testClassNames) {
        this.testClassNames = testClassNames;
    }

}
