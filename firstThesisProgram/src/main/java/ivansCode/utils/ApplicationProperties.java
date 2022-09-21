package ivansCode.utils;

import ivansCode.components.Project;
import ivansCode.components.techniques.Technique;
import ivansCode.components.techniques.TechniqueFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;

public final class ApplicationProperties {

    private ApplicationProperties(){}

    private static Properties properties;

    public static void readApplicationProperties() throws IOException {
        properties = new Properties();
        InputStream in = ApplicationProperties.class.getClassLoader()
                .getResourceAsStream("properties.properties");
        assert in != null;
        properties.load(in);
        in.close();
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static List<Project> getProjects() throws IOException {
        String[] projectArr = getProperty("projectList").split(";");
        List<Project> projects = new ArrayList<>();
        for (String projectName : projectArr){
            String className = getProperty(String.format("projects.%s.className", projectName));
            String simpleClassName = getProperty(String.format("projects.%s.simpleClassName", projectName));
            Path sourceCodePath = Path.of(Objects.requireNonNull(ApplicationProperties.class.getClassLoader()
                    .getResource(getProperty(String.format("projects.%s.sourceCodePath", projectName)))).getPath());
            Path targetCodePath = Path.of(Objects.requireNonNull(ApplicationProperties.class.getClassLoader()
                    .getResource(getProperty(String.format("projects.%s.targetCodePath", projectName)))).getPath());
            Path projectPath = Path.of(Objects.requireNonNull(ApplicationProperties.class.getClassLoader()
                    .getResource(getProperty(String.format("projects.%s.projectPath", projectName)))).getPath());
            Set<String> testClassNames = new HashSet<>();
            int numTestClasses = Integer.parseInt(getProperty(String.format("projects.%s.numTestCases", projectName)));
            for (int i = 0; i < numTestClasses; i++){
                testClassNames.add(getProperty(String.format("projects.%s.testCases.%d.methodName", projectName, i)));
            }
            projects.add(new Project(
                    className, simpleClassName, sourceCodePath, targetCodePath, projectPath, testClassNames));
        }
        return projects;
    }

    public static List<TechniqueFactory<? extends Technique>> getFactories() throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String[] techniqueArr = getProperty("techniqueList").split(";");
        List<TechniqueFactory<? extends Technique>> factories = new ArrayList<>();
        for (String techniqueName : techniqueArr){
            String factoryName = getProperty(String.format("techniques.%s.factoryName", techniqueName));
            Class<?> factoryClass = Class.forName(factoryName);
            factories.add((TechniqueFactory<? extends Technique>) factoryClass.getConstructor().newInstance());
        }
        return factories;
    }

    public static Path getTempPath(){
        return Path.of(getProperty("tempPath"));
    }

    public static Path getOutputPath(){
        return Path.of(getProperty("outputPath"));
    }

}
