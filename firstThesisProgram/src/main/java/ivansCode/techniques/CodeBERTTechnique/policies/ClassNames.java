package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.NodeUtils;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;
import ivansCode.utils.StringUtils;

import java.util.List;

public class ClassNames extends CommonPolicy {

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        Node enclosingClassExpression = NodeUtils.enclosingClassExpression(node);
        if (enclosingClassExpression != null){
            return NodeUtils.startsWith(enclosingClassExpression, node);
        } else {
            return false;
        }
    }

    @Override
    public Node getTrueNode(Node node){
        return NodeUtils.enclosingClassExpression(node);
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        Node enclosingClassExpression = NodeUtils.enclosingClassExpression(node);
        if (NodeUtils.nodesEqualByString(node, enclosingClassExpression)){
            return getLeftOfTokenRange(node.getTokenRange().get());
        } else {
            return getRightOfTokenRange(node.getTokenRange().get());
        }
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        Node enclosingClassExpression = NodeUtils.enclosingClassExpression(node);
        return getRightOfTokenRange(enclosingClassExpression.getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        Node enclosingClassExpression = NodeUtils.enclosingClassExpression(node);
        if (NodeUtils.nodesEqualByString(node, enclosingClassExpression)){
            return NodeUtils.nodeToString(node);
        } else {
            String enclosingStr = NodeUtils.nodeToString(enclosingClassExpression);
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
        String prefix = (nodeStr.equals(NodeUtils.nodeToString(NodeUtils.enclosingClassExpression(node))) ? ""
                : nodeStr);
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
