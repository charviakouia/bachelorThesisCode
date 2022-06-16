package ivansCode.components;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
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
        this.id = idCounter++;
        this.sourceCode = StaticJavaParser.parse(sourceCode).toString();
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
            return sourceCode.equals(mutant.sourceCode);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCode);
    }

    @Override
    public int compareTo(Mutant o) {
        return CharSequence.compare(sourceCode, o.sourceCode);
    }

    public int getId() {
        return id;
    }

}
