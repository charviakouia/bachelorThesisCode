package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;

import java.util.List;
import java.util.Set;

public class BinaryBitwise extends CommonPolicy {

    private final Set<BinaryExpr.Operator> allowedOperators = Set.of(
            BinaryExpr.Operator.BINARY_AND,
            BinaryExpr.Operator.BINARY_OR,
            BinaryExpr.Operator.XOR,
            BinaryExpr.Operator.LEFT_SHIFT,
            BinaryExpr.Operator.SIGNED_RIGHT_SHIFT,
            BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT
    );

    private final Set<String> allowedReplacements = Set.of(
            "&", "|", "^", "<<", ">>", ">>>"
    );

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        return node instanceof BinaryExpr && allowedOperators.contains(((BinaryExpr) node).getOperator());
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLastOfTokenRange(((BinaryExpr) node).getLeft().getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getFirstOfTokenRange(((BinaryExpr) node).getRight().getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        return ((BinaryExpr) node).getOperator().asString();
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        filterOptionsByInclusionInSet(options, allowedReplacements);
    }

}
