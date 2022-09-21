package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.UnaryExpr;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Set;

public class IncrementsPresent extends CommonPolicy {

    private final Set<UnaryExpr.Operator> allowedOperators = Set.of(
            UnaryExpr.Operator.PREFIX_INCREMENT,
            UnaryExpr.Operator.POSTFIX_INCREMENT,
            UnaryExpr.Operator.PREFIX_DECREMENT,
            UnaryExpr.Operator.POSTFIX_DECREMENT
    );

    private final Set<String> allowedReplacements = Set.of(
            "++", "--", ""
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        return node instanceof UnaryExpr && allowedOperators.contains(((UnaryExpr) node).getOperator());
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        UnaryExpr.Operator operator = ((UnaryExpr) node).getOperator();
        if (Set.of(UnaryExpr.Operator.PREFIX_DECREMENT, UnaryExpr.Operator.PREFIX_INCREMENT).contains(operator)){
            return getLeftOfTokenRange(node.getTokenRange().get());
        } else if (Set.of(UnaryExpr.Operator.POSTFIX_DECREMENT, UnaryExpr.Operator.POSTFIX_INCREMENT).contains(operator)){
            return getLastOfTokenRange(((UnaryExpr) node).getExpression().getTokenRange().get());
        } else {
            throw new IllegalArgumentException("Operator neither ++ nor --");
        }
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        UnaryExpr.Operator operator = ((UnaryExpr) node).getOperator();
        if (Set.of(UnaryExpr.Operator.PREFIX_DECREMENT, UnaryExpr.Operator.PREFIX_INCREMENT).contains(operator)){
            return getFirstOfTokenRange(((UnaryExpr) node).getExpression().getTokenRange().get());
        } else if (Set.of(UnaryExpr.Operator.POSTFIX_DECREMENT, UnaryExpr.Operator.POSTFIX_INCREMENT).contains(operator)){
            return getRightOfTokenRange(node.getTokenRange().get());
        } else {
            throw new IllegalArgumentException("Operator neither ++ nor --");
        }
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
