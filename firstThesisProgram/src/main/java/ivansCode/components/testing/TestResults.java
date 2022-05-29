package ivansCode.components.testing;

import ivansCode.components.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TestResults {

    private final Map<String, Boolean> passedMap = new HashMap<>();

    public void setPassed(String testName, boolean passed){
        passedMap.put(testName, passed);
    }

    @Override
    public String toString() {
        return passedMap.toString();
    }
}
