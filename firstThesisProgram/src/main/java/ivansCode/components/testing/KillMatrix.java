package ivansCode.components.testing;

import ivansCode.components.Mutant;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.K;

import java.util.*;

public class KillMatrix {

    public static final String FILE_NAME = "matrix";
    public static final String FILE_TYPE = ".txt";

    private final Map<Pair<String, Mutant>, Boolean> killMatrixMap = new TreeMap<>();
    private final Set<String> testNames = new TreeSet<>();
    private final Set<Mutant> mutants = new TreeSet<>();

    private final static int cellSize = 10;

    public void addEntry(String testName, Mutant mutant, boolean killed){
        Pair<String, Mutant> pair = new ImmutablePair<>(testName, mutant);
        killMatrixMap.put(pair, killed);
        testNames.add(testName);
        mutants.add(mutant);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String[] testNamesArr = testNames.toArray(new String[0]);
        Mutant[] mutantsArr = mutants.toArray(new Mutant[0]);
        builder.append(" ".repeat(cellSize));
        for (Mutant value : mutantsArr) {
            String intValue = Integer.toString(value.getId());
            builder.append("|").append(" ".repeat(Math.max(0, cellSize - intValue.length()))).append(intValue);
        }
        builder.append("|").append(System.getProperty("line.separator"));
        builder.append("-".repeat((mutantsArr.length + 1) * (cellSize + 1))).append(System.getProperty("line.separator"));
        for (String testName : testNamesArr){
            String suffix = testName.substring(Math.max(testName.length() - cellSize, 0));
            builder.append(" ".repeat(cellSize - suffix.length())).append(suffix);
            for (Mutant mutant : mutantsArr){
                builder.append("|").append((killMatrixMap.get(new ImmutablePair<>(testName, mutant)) ? "X".repeat(cellSize) : " ".repeat(cellSize)));
            }
            builder.append("|").append(System.getProperty("line.separator"));
            builder.append("-".repeat((mutantsArr.length + 1) * (cellSize + 1))).append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    public static void main(String[] args){

        KillMatrix killMatrix = new KillMatrix();
        System.out.println(killMatrix.toString());

    }

}
