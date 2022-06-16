package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import ivansCode.components.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.TokenParser;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface Policy {

    boolean isMatch(Node node);

    JavaToken getLeftOfMasked(Node node, TokenParser parser);

    JavaToken getRightOfMasked(Node node, TokenParser parser);

    String getCenterString(Node node);

    Pair<Integer, Integer> getDivision(int numTokensLeft, int numTokensRight);

    void filterOptions(List<CodeBERTOption> options, String originalToken);

}
