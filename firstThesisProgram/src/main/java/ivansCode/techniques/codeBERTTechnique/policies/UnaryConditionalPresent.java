package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.UnaryExpr;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

public class UnaryConditionalPresent extends CommonPolicy {

    private final Set<UnaryExpr.Operator> allowedOperators = Set.of(
            UnaryExpr.Operator.LOGICAL_COMPLEMENT
    );

    private final Set<String> allowedReplacements = Set.of(
            "!", ""
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        return node instanceof UnaryExpr && allowedOperators.contains(((UnaryExpr) node).getOperator());
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLeftOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getFirstOfTokenRange(((UnaryExpr) node).getExpression().getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        return ((UnaryExpr) node).getOperator().asString();
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        filterOptionsByInclusionInSet(options, allowedReplacements);
    }

    @Override
    public void addOptions(List<CodeBERTOption> options, String beforeSequence, String afterSequence) {
        Assertions.assertEquals(0, options.stream().filter(o -> o.getTokenString().isBlank()).count());
        includeOption(options, beforeSequence + afterSequence, "", 0.1);
    }

}
