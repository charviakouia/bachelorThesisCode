package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.UnaryExpr;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

public class UnaryBitwisePresent extends CommonPolicy {

    private final Set<UnaryExpr.Operator> allowedOperators = Set.of(
            UnaryExpr.Operator.BITWISE_COMPLEMENT
    );

    private final Set<String> allowedReplacements = Set.of(
            "~", ""
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
        if (options.stream().noneMatch(o -> o.getTokenString().isBlank())){
            includeOption(options, beforeSequence + afterSequence, "", 0.1);
        }
    }

}
