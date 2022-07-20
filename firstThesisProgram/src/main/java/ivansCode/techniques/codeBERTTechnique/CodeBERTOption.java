package ivansCode.techniques.codeBERTTechnique;

public class CodeBERTOption implements Cloneable {

    private double score;
    private int token;
    private String tokenString;
    private String sequence;

    public CodeBERTOption(double score, int token, String tokenString, String sequence) {
        this.score = score;
        this.token = token;
        this.tokenString = tokenString;
        this.sequence = sequence;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "CodeBERTOption{" +
                "score=" + score +
                ", token=" + token +
                ", tokenString='" + tokenString + '\'' +
                ", sequence='" + sequence + '\'' +
                '}';
    }

    @Override
    public CodeBERTOption clone() {
        try {
            CodeBERTOption clone = (CodeBERTOption) super.clone();
            clone.score = score;
            clone.token = token;
            clone.tokenString = tokenString;
            clone.sequence = sequence;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
