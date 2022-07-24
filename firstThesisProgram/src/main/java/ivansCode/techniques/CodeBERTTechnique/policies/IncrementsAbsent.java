package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.NodeUtils;
import ivansCode.techniques.CodeBERTTechnique.components.ParsedExpression;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;

import java.util.List;
import java.util.Set;

public abstract class IncrementsAbsent extends CommonPolicy {

    protected final Set<UnaryExpr.Operator> forbiddenOperators = Set.of(
            UnaryExpr.Operator.PREFIX_INCREMENT,
            UnaryExpr.Operator.POSTFIX_INCREMENT,
            UnaryExpr.Operator.PREFIX_DECREMENT,
            UnaryExpr.Operator.POSTFIX_DECREMENT
    );

    protected final Set<String> allowedReplacements = Set.of(
            "++", "--", ""
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        ParsedExpression parsedExpression = new ParsedExpression(node, typeMap);
        boolean isNonIncrementedRHSVariable =
                parsedExpression.isRHSVariable() &&
                !(NodeUtils.followsHierarchy(NodeUtils.up(node, 1), NameExpr.class, UnaryExpr.class)
                        && NodeUtils.unaryOperatorIn((UnaryExpr) NodeUtils.up(node, 2), forbiddenOperators));
        return isNonIncrementedRHSVariable && parsedExpression.getType().isNumericalType();
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        filterOptionsByInclusionInSet(options, allowedReplacements);
    }

    @Override
    public String getCenterString(Node node) {
        return "";
    }

}
