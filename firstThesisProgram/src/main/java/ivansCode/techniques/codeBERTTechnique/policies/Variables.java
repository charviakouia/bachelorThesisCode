package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.NodeUtils;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.ParsedExpression;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
import ivansCode.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class Variables extends CommonPolicy {

    @Override
    public Node getTrueNode(Node node){
        return NodeUtils.enclosingRHSVariable(node);
    }

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        Node enclosingRHSVariable = NodeUtils.enclosingRHSVariable(node);
        if (enclosingRHSVariable != null){
            return NodeUtils.startsWith(enclosingRHSVariable, node);
        } else {
            return false;
        }
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        Node enclosingRHSVariable = NodeUtils.enclosingRHSVariable(node);
        if (NodeUtils.nodesEqualByString(node, enclosingRHSVariable)){
            return getLeftOfTokenRange(node.getTokenRange().get());
        } else {
            return getRightOfTokenRange(node.getTokenRange().get());
        }
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        Node enclosingRHSVariable = NodeUtils.enclosingRHSVariable(node);
        return getRightOfTokenRange(enclosingRHSVariable.getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        Node enclosingRHSVariable = NodeUtils.enclosingRHSVariable(node);
        if (NodeUtils.nodesEqualByString(node, enclosingRHSVariable)){
            return NodeUtils.nodeToString(node);
        } else {
            String enclosingStr = NodeUtils.nodeToString(enclosingRHSVariable);
            String nodeStr = NodeUtils.nodeToString(node);
            return StringUtils.replaceFirst(enclosingStr, nodeStr, "");
        }
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {}

    /*
    Possible extension: Filter by whether the replacement type extends the original type
    @Override
    public void filterOptions(List<CodeBERTOption> options, Node node, VariableTypeMap typeMap) {
        ParsedExpression nodeExpr = new ParsedExpression(node, typeMap);
        String nodeStr = NodeUtils.nodeToString(node);
        String prefix = (nodeStr.equals(NodeUtils.nodeToString(NodeUtils.enclosingRHSVariable(node))) ? "" : nodeStr);
        List<CodeBERTOption> toRemove = new LinkedList<>();
        for (CodeBERTOption option : options){
            ParsedExpression replacedExpr = new ParsedExpression(prefix + option.getTokenString(), typeMap);
            if (!replacedExpr.getType().isUndefined() && !nodeExpr.getType().isUndefined()
                    && !replacedExpr.getType().equals(nodeExpr.getType())){
                toRemove.add(option);
            }
        }
        filterOptionsByRemoveSet(options, toRemove);
    }
     */

}
