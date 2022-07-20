package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.LiteralExpr;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.ParsedType;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
import ivansCode.utils.JavaLiteralType;

import java.util.LinkedList;
import java.util.List;

public class Constants extends CommonPolicy {

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        return node instanceof LiteralExpr;
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLeftOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getRightOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        return node.toString();
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        List<CodeBERTOption> toRemove = new LinkedList<>();
        ParsedType originalType = new ParsedType((LiteralExpr) node);
        for (CodeBERTOption option : options){
            ParsedType currentType = new ParsedType(option.getTokenString());
            if (currentType.isUndefined() || !currentType.equals(originalType)){
                toRemove.add(option);
            }
        }
        filterOptionsByRemoveSet(options, toRemove);
    }

}
