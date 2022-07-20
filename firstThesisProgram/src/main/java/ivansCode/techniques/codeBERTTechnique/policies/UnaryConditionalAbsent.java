package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.type.Type;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.NodeUtils;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.ParsedExpression;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;

import java.util.List;
import java.util.Set;

public class UnaryConditionalAbsent extends CommonPolicy {

    private final Set<UnaryExpr.Operator> forbiddenOperators = Set.of(
            UnaryExpr.Operator.LOGICAL_COMPLEMENT
    );

    private final Set<String> allowedReplacements = Set.of(
            "!", ""
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        ParsedExpression parsedExpression = new ParsedExpression(node, typeMap);
        boolean isNonNegatedRHSVariable =
                parsedExpression.isRHSVariable() &&
                !(NodeUtils.followsHierarchy(NodeUtils.up(node, 1), NameExpr.class, UnaryExpr.class)
                        && NodeUtils.unaryOperatorIn((UnaryExpr) NodeUtils.up(node, 2), forbiddenOperators));
        return isNonNegatedRHSVariable && parsedExpression.getType().isBooleanType();
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLeftOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getFirstOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        return "";
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        filterOptionsByInclusionInSet(options, allowedReplacements);
    }

}