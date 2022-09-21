package ivansCode.utils;

import ivansCode.components.ProjectTestSuite;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public final class ApplicationProperties {

    private ApplicationProperties(){}

    private static Properties properties;

    public static void readApplicationProperties() throws IOException {
        properties = new Properties();
        InputStream in = ApplicationProperties.class.getClassLoader().getResourceAsStream("properties.properties");
        assert in != null;
        properties.load(in);
        in.close();
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static List<ProjectTestSuite> readProjectTestSuites(){
        List<ProjectTestSuite> result = new LinkedList<>();
        String[] projectArr = getProperty("projectList").split(";");
        for (String projectName : projectArr){
            int numTotalTestCases =
                    Integer.parseInt(getProperty(String.format("projects.%s.numTestCases", projectName)));
            Set<String> allTestCases = new HashSet<>();
            for (int i = 0; i < numTotalTestCases; i++){
                allTestCases.add(getProperty(String.format("projects.%s.testCases.%d.methodName", projectName, i)));
            }
            int numGroups = Integer.parseInt(getProperty(String.format("groupings.%s.numGroups", projectName)));
            Map<String, Set<String>> groupNameToGroup = new HashMap<>();
            for (int i = 0; i < numGroups; i++){
                String groupName = getProperty(String.format("groupings.%s.%d.name", projectName, i));
                int numTestCasesInGroup =
                        Integer.parseInt(getProperty(String.format("groupings.%s.%d.numTestCases", projectName, i)));
                List<String> groupedTestCases = new LinkedList<>();
                for (int j = 0; j < numTestCasesInGroup; j++){
                    String testName = getProperty(String.format("groupings.%s.%d.%d.methodName", projectName, i, j));
                    if (allTestCases.contains(testName)){
                        groupedTestCases.add(testName);
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Unknown test %s found in group %s in project %s",
                                        testName, groupName, projectName));
                    }
                }
                groupNameToGroup.put(groupName, new HashSet<>(groupedTestCases));
            }
            String dirName = getProperty(String.format("projects.%s.dirName", projectName));
            result.add(new ProjectTestSuite(dirName, projectName, allTestCases, groupNameToGroup));
        }
        return result;
    }

    public static Path getTempPath(){
        return Path.of(properties.getProperty("tempPath"));
    }

    public static Path getDataPath(){
        return Path.of(properties.getProperty("dataPath"));
    }

}
