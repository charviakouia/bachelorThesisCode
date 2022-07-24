package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.NodeUtils;
import ivansCode.techniques.CodeBERTTechnique.components.ParsedExpression;
import ivansCode.techniques.CodeBERTTechnique.components.VariableTypeMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NumericalExpressions extends CommonPolicy {

    @Override
    public boolean isMatch(Node node, VariableTypeMap typeMap) {
        ParsedExpression parsedExpr = new ParsedExpression(node, typeMap);
        if (!parsedExpr.isLHSVariable() && !parsedExpr.getType().isUndefined() &&
                parsedExpr.getType().isNumericalType()){
            Node parentNode = NodeUtils.up(node, 1);
            if (parentNode != null){
                ParsedExpression parsedParentExpr =
                        new ParsedExpression(parentNode, typeMap, Map.of(node, parsedExpr.getType()));
                return !parsedParentExpr.getType().isNumericalType();
            } else {
                return true;
            }
        } else {
            return false;
        }
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
        // Possible extension: Filter by whether the replacement type extends the original type
        List<CodeBERTOption> toRemove = new LinkedList<>();
        for (CodeBERTOption option : options){
            try {
                ParsedExpression parsedExpression = new ParsedExpression(option.getSequence(), typeMap);
                if (!parsedExpression.getType().isUndefined() && !parsedExpression.getType().isNumericalType()){
                    toRemove.add(option);
                }
            } catch (ParseProblemException e){
                toRemove.add(option);
            }
        }
        filterOptionsByRemoveSet(options, toRemove);
    }

    @Override
    public void correctOptions(List<CodeBERTOption> options, String beforeNode, String beforeMasked,
                               String afterMasked, String afterNode) {
        for (CodeBERTOption option : options){
            assert beforeMasked.isBlank() && afterMasked.isBlank();
            if (option.getTokenString().split("\\s+").length >= 2){
                option.setSequence(String.format("%s (%s) %s", beforeNode, option.getTokenString(), afterNode));
            }
        }
    }

}
