package ivansCode.components;

import ivansCode.utils.CustomAgent;

import java.util.Objects;

public class Mutant implements Comparable<Mutant> {

    private static int idCounter = 0;

    private final int id;
    private final Class<?> originalClass;
    private final byte[] mutatedBytes;
    private final String sourceCode;

    public Mutant(Class<?> originalClass, byte[] mutatedBytes, String sourceCode) {
        this.originalClass = originalClass;
        this.mutatedBytes = mutatedBytes;
        this.sourceCode = sourceCode;
        this.id = ++idCounter;
    }

    public boolean install(){
        return CustomAgent.introduceMutation(originalClass, mutatedBytes);
    }

    @Override
    public String toString() {
        return sourceCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Mutant mutant = (Mutant) o;
            return id == mutant.id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Mutant o) {
        return Integer.compare(id, o.id);
    }

    public int getId() {
        return id;
    }

}
