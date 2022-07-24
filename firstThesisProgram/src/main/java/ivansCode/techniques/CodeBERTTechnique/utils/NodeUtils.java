package ivansCode.techniques.CodeBERTTechnique.utils;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import ivansCode.techniques.CodeBERTTechnique.components.ParsedExpression;

import java.util.Collection;
import java.util.Optional;

public final class NodeUtils {

    public static boolean followsHierarchy(Node node, Class<?>... classes){
        Node currentNode = node;
        boolean followsHierarchy = true;
        int classArrayIndex = 0;
        while (followsHierarchy && classArrayIndex < classes.length){
            if (currentNode != null){
                Class<?> clazz = classes[classArrayIndex];
                followsHierarchy = node.getClass().equals(clazz);
                currentNode = currentNode.getParentNode().orElse(null);
                classArrayIndex++;
            } else {
                followsHierarchy = false;
            }
        }
        return followsHierarchy;
    }

    public static boolean unaryOperatorIn(UnaryExpr node, Collection<UnaryExpr.Operator> operators){
        return operators.contains(node.getOperator());
    }

    public static boolean binaryOperatorIn(BinaryExpr node, Collection<BinaryExpr.Operator> operators) {
        return operators.contains(node.getOperator());
    }

    public static Node up(Node node, int numLevels){
        Node result = node;
        for (int i = 0; i < numLevels && result != null; i++){
            result = result.getParentNode().orElse(null);
        }
        return result;
    }

    public static Node enclosingRHSVariable(Node node){
        Node currentNode = node;
        while (currentNode != null){
            ParsedExpression parsedExpression = new ParsedExpression(currentNode);
            if (parsedExpression.isOpenName()){
                if (parsedExpression.isRHSVariable()){
                    return currentNode;
                } else {
                    currentNode = currentNode.getParentNode().orElse(null);
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static Node enclosingMethodCall(Node node){
        Node currentNode = node;
        while (currentNode != null){
            ParsedExpression parsedExpression = new ParsedExpression(currentNode);
            if (parsedExpression.isOpenName()){
                if (parsedExpression.isMethodCall()){
                    return currentNode;
                } else {
                    currentNode = currentNode.getParentNode().orElse(null);
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static Node enclosingClassExpression(Node node){
        Node currentNode = node;
        while (currentNode != null){
            ParsedExpression parsedExpression = new ParsedExpression(currentNode);
            if (parsedExpression.isOpenName()){
                if (parsedExpression.isClassExpression()){
                    return currentNode;
                } else {
                    currentNode = currentNode.getParentNode().orElse(null);
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static boolean startsWith(Node wholeNode, Node partNode){
        Node currentNode = wholeNode;
        while (currentNode != null){
            if (currentNode.equals(partNode)){
                return true;
            } else if (currentNode instanceof Name){
                currentNode = ((Name) currentNode).getQualifier().orElse(null);
            } else if (currentNode instanceof FieldAccessExpr){
                currentNode = ((FieldAccessExpr) currentNode).getScope();
            } else if (currentNode instanceof NameExpr){
                currentNode = ((NameExpr) currentNode).getName();
            } else if (currentNode instanceof ClassOrInterfaceType) {
                currentNode = ((ClassOrInterfaceType) currentNode).getScope().orElse(null);
            } else if (currentNode instanceof MethodCallExpr){
                currentNode = ((MethodCallExpr) currentNode).getScope().orElse(null);
            } else {
                return false;
            }
        }
        return false;
    }

    public static JavaToken nextNonBlankToken(JavaToken token, boolean right) {
        if (right) {
            Optional<JavaToken> optionalNext = Optional.of(token);
            while (optionalNext.isPresent() && optionalNext.get().asString().isBlank()) {
                optionalNext = optionalNext.get().getNextToken();
            }
            return optionalNext.orElse(null);
        } else {
            Optional<JavaToken> optionalPrevious = Optional.of(token);
            while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank()) {
                optionalPrevious = optionalPrevious.get().getPreviousToken();
            }
            return optionalPrevious.orElse(null);
        }
    }

    public static boolean nodesEqualByString(Node a, Node b){
        return a.toString().replaceAll("\\s+", " ").trim()
                .equals(b.toString().replaceAll("\\s+", " ").trim());
    }

    public static String nodeToString(Node a){
        return a.toString().replaceAll("\\s+", " ").trim();
    }

    /*
    public static void main(String[] args){
        String code = "class A { int c = 5; Object a = c * ((-c) - 7) + 8; }";
        CompilationUnit unit = StaticJavaParser.parse(code);
        List<Node> nodes = unit.stream(Node.TreeTraversal.PREORDER).toList();
        for (Node node : nodes){
            System.out.printf("Name '%s', value '%s'%s", node.getClass().getSimpleName(),
                    NodeUtils.nodeToString(node), System.lineSeparator());
        }
    }
     */

}
