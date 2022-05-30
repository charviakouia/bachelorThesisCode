package ivansCode.components;

import ivansCode.utils.CustomAgent;

import java.util.Objects;

public class Mutant implements Comparable<Mutant> {

    private final Class<?> originalClass;
    private final byte[] mutatedBytes;
    private final String sourceCode;

    public Mutant(Class<?> originalClass, byte[] mutatedBytes, String sourceCode) {
        this.originalClass = originalClass;
        this.mutatedBytes = mutatedBytes;
        this.sourceCode = sourceCode;
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
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Mutant mutant = (Mutant) o;
            return originalClass.equals(mutant.originalClass) && sourceCode.equals(mutant.sourceCode);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalClass, sourceCode);
    }

    @Override
    public int compareTo(Mutant o) {
        if (!originalClass.equals(o.originalClass)){
            return originalClass.getName().compareTo(o.originalClass.getName());
        } else if (!sourceCode.equals(o.sourceCode)) {
            return sourceCode.compareTo(o.sourceCode);
        } else {
            return 0;
        }
    }
}
