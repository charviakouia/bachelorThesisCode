package ivansCode.components.testing;

import ivansCode.components.Mutant;

import java.util.HashMap;
import java.util.Map;

public class TestResults {

    private final Map<String, Boolean> passedMap = new HashMap<>();

    public void setPassed(String testName, boolean passed){
        passedMap.put(testName, passed);
    }

    @Override
    public String toString() {
        return passedMap.toString();
    }

    public void writeTo(KillMatrix matrix, Mutant mutant){
        for (Map.Entry<String, Boolean> entry : passedMap.entrySet()){
            matrix.addEntry(entry.getKey(), mutant, !entry.getValue());
        }
    }

}
