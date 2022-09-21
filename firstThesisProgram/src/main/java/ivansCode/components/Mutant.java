package ivansCode.components;

import com.github.javaparser.StaticJavaParser;

import java.util.Objects;

public class Mutant implements Comparable<Mutant> {

    private static int idCounter = 2;

    private final int id;
    private final String subjectClassName;
    private final byte[] mutatedBytes;
    private final String mutatedSourceCode;
    private String generatedBy = "";

    public Mutant(String subjectClassName, byte[] mutatedBytes, String mutatedSourceCode) {
        this.subjectClassName = subjectClassName;
        this.mutatedBytes = mutatedBytes;
        this.id = idCounter++;
        this.mutatedSourceCode = StaticJavaParser.parse(mutatedSourceCode).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Mutant mutant = (Mutant) o;
            return mutatedSourceCode.equals(mutant.mutatedSourceCode);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mutatedSourceCode);
    }

    @Override
    public int compareTo(Mutant o) {
        return CharSequence.compare(mutatedSourceCode, o.mutatedSourceCode);
    }

    @Override
    public String toString() {
        return mutatedSourceCode;
    }

    public int getId() {
        return id;
    }

    public byte[] getMutatedBytes() {
        return mutatedBytes;
    }

    public String getSubjectClassName() {
        return subjectClassName;
    }

    public String getMutatedSourceCode() {
        return mutatedSourceCode;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

}
