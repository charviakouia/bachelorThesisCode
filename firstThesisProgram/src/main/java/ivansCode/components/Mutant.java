package ivansCode.components;

import ivansCode.utils.CustomAgent;

public class Mutant {

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

}
