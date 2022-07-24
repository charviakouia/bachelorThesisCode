package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.NodeUtils;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;
import ivansCode.utils.StringUtils;

import java.util.List;

public class MethodCalls extends CommonPolicy {

    @Override
    public Node getTrueNode(Node node){
        return NodeUtils.enclosingMethodCall(node);
    }

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        Node enclosingMethodCall = NodeUtils.enclosingMethodCall(node);
        if (enclosingMethodCall != null){
            return NodeUtils.startsWith(enclosingMethodCall, node);
        } else {
            return false;
        }
    }

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        Node enclosingMethodCall = NodeUtils.enclosingMethodCall(node);
        if (NodeUtils.nodesEqualByString(node, enclosingMethodCall)){
            return getLeftOfTokenRange(node.getTokenRange().get());
        } else {
            return getRightOfTokenRange(node.getTokenRange().get());
        }
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        Node enclosingMethodCall = NodeUtils.enclosingMethodCall(node);
        return getRightOfTokenRange(enclosingMethodCall.getTokenRange().get());
    }

    @Override
    public String getCenterString(Node node) {
        Node enclosingMethodCall = NodeUtils.enclosingMethodCall(node);
        if (NodeUtils.nodesEqualByString(node, enclosingMethodCall)){
            return NodeUtils.nodeToString(node);
        } else {
            String enclosingStr = NodeUtils.nodeToString(enclosingMethodCall);
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
        String prefix = (nodeStr.equals(NodeUtils.nodeToString(NodeUtils.enclosingMethodCall(node))) ? "" : nodeStr);
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
