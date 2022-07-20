package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AssignExpr;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;

import java.util.List;
import java.util.Set;

public class BinaryAssignment extends CommonPolicy {

    private final Set<AssignExpr.Operator> allowedOperators = Set.of(
            AssignExpr.Operator.PLUS,
            AssignExpr.Operator.MINUS,
            AssignExpr.Operator.MULTIPLY,
            AssignExpr.Operator.DIVIDE,
            AssignExpr.Operator.REMAINDER,
            AssignExpr.Operator.SIGNED_RIGHT_SHIFT,
            AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT,
            AssignExpr.Operator.LEFT_SHIFT,
            AssignExpr.Operator.BINARY_AND,
            AssignExpr.Operator.BINARY_OR,
            AssignExpr.Operator.XOR,
            AssignExpr.Operator.ASSIGN
    );

    private final Set<String> allowedReplacements = Set.of(
            "+=", "-=", "*=", "/=", "%=", ">>=", ">>>=", "<<=", "&=", "|=", "^=", "="
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        return node instanceof AssignExpr && allowedOperators.contains(((AssignExpr) node).getOperator());
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLastOfTokenRange(((AssignExpr) node).getTarget().getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getFirstOfTokenRange(((AssignExpr) node).getValue().getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        return ((AssignExpr) node).getOperator().asString();
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        filterOptionsByInclusionInSet(options, allowedReplacements);
    }

}
