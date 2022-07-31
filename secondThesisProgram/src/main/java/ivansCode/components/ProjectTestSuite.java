package ivansCode.components;

import utils.ApplicationProperties;

import java.nio.file.Path;
import java.util.*;

public class ProjectTestSuite {

    private final String dirName;
    private final String projectName;
    private final Set<String> tests;
    private final Map<String, Set<String>> groupNameToGroup;

    public ProjectTestSuite(String dirName, String projectName, Collection<String> tests,
                            Map<String, Set<String>> groupNameToGroup){

        this.dirName = dirName;
        this.projectName = projectName;
        this.tests = new HashSet<>(tests);
        this.groupNameToGroup = new HashMap<>(groupNameToGroup);
        Set<String> testsFromMap = new HashSet<>();
        for (Set<String> testSet : groupNameToGroup.values()){
            testsFromMap.addAll(testSet);
        }
        for (String test : testsFromMap){
            if (!this.tests.contains(test)){
                throw new IllegalArgumentException();
            }
        }

    }

    public Set<String> getTests() {
        return tests;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDirName() {
        return dirName;
    }

    public Set<String> getTestGroup(String groupName){
        return groupNameToGroup.get(groupName);
    }

    public Path getPathToProject(){
        return ApplicationProperties.getDataPath().resolve(dirName);
    }

}
