package ivansCode.components;

import com.google.common.io.Files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Project {

    private final Class<?> subjectClass;
    private final Path sourceCodePath;
    private final String[] testClassNames;

    public Project(Class<?> subjectClass, Path sourceCodePath, String[] testClassNames) {
        this.subjectClass = subjectClass;
        this.sourceCodePath = sourceCodePath;
        this.testClassNames = testClassNames;
    }

    public String getOriginalSourceCode() throws IOException {
        return String.join("", Files.readLines(sourceCodePath.toFile(), StandardCharsets.UTF_8));
    }

    public Class<?> getSubjectClass() {
        return subjectClass;
    }

    public Path getSourceCodePath() {
        return sourceCodePath;
    }

    public String[] getTestClassNames() {
        return testClassNames;
    }

}
