package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface Policy {

    boolean isMatch(Node node, VariableTypeMap typeMap);

    Node getTrueNode(Node node);

    JavaToken getLeftOfMasked(Node node);

    JavaToken getRightOfMasked(Node node);

    String getCenterString(Node node);

    Pair<Integer, Integer> getDivision(int numTokensLeft, int numTokensRight);

    void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap);

    void addOptions(List<CodeBERTOption> options, String beforeSequence, String afterSequence);

    void correctOptions(List<CodeBERTOption> options, String beforeNode, String beforeMasked, String afterMasked,
                        String afterNode);

}
