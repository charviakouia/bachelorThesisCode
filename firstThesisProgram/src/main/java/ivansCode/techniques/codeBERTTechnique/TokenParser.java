package ivansCode.techniques.codeBERTTechnique;

import com.github.javaparser.JavaToken;

public interface TokenParser {

    JavaToken rewindToken(JavaToken token, boolean right);

}
