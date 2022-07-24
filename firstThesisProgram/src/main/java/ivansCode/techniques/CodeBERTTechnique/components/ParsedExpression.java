package ivansCode.techniques.CodeBERTTechnique.components;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import ivansCode.utils.JavaLiteralType;

import java.util.*;

public class ParsedExpression {

    private boolean isOpenName = false;
    private boolean isRHSVariable = false;
    private boolean isMethodCall = false;
    private boolean isClassExpression = false;
    private boolean isLHSVariable = false;
    private String name = null;
    private ParsedType type = null;

    public ParsedExpression(Node node, VariableTypeMap typeMap, Map<Node, ParsedType> trustedNodesMap){
        isOpenName = determineWhetherNodeIsOpenName(node);
        isRHSVariable = determineWhetherNodeIsRSHVariable(node);
        isMethodCall = determineWhetherNodeIsMethodCall(node);
        isClassExpression = determineWhetherNodeIsClassExpr(node);
        isLHSVariable = determineWhetherNodeIsLHSVariable(node);
        if (isRHSVariable){
            name = determineVariableName(node);
            if (trustedNodesMap.containsKey(node)){
                type = trustedNodesMap.get(node);
            } else {
                type = (name != null ? typeMap.getType(name, node) : ParsedType.getUndefined());
                type = (type == null ? ParsedType.getUndefined() : type);
            }
        } else if (isMethodCall){
            name = determineMethodCallName(node, typeMap);
            if (trustedNodesMap.containsKey(node)){
                type = trustedNodesMap.get(node);
            } else {
                type = (name != null ? typeMap.getType(name, node) : ParsedType.getUndefined());
                type = (type == null ? ParsedType.getUndefined() : type);
            }
        } else if (isClassExpression) {
            name = determineClassExprName(node);
            type = ParsedType.getUndefined();
        } else {
            type = (type == null ? parseArithmeticExpr(node, typeMap, trustedNodesMap) : type);
            type = (type == null ? parseBooleanExpr(node, typeMap, trustedNodesMap) : type);
            type = (type == null ? ParsedType.getUndefined() : type);
        }
    }

    public ParsedExpression(Node node, VariableTypeMap typeMap){
        this(node, typeMap, new HashMap<>());
    }

    public ParsedExpression(Node node){
        isOpenName = determineWhetherNodeIsOpenName(node);
        isRHSVariable = determineWhetherNodeIsRSHVariable(node);
        isMethodCall = determineWhetherNodeIsMethodCall(node);
        isClassExpression = determineWhetherNodeIsClassExpr(node);
        isLHSVariable = determineWhetherNodeIsLHSVariable(node);
        if (isRHSVariable){
            name = determineVariableName(node);
            type = ParsedType.getUndefined();
        } else if (isClassExpression){
            name = determineClassExprName(node);
            type = ParsedType.getUndefined();
        } else {
            type = ParsedType.getUndefined();
        }

    }

    public ParsedExpression(String exprStr, VariableTypeMap typeMap){
        this(getNodeFromString(exprStr), typeMap);
    }

    private static Node getNodeFromString(String exprStr){
        String unlikelyClassName = "ABCABCJJIEJAQW";
        String unlikelyVariableName = "dkeewsdfbbsek";
        String code = String.format("class %s { Object %s = %s; }", unlikelyClassName, unlikelyVariableName,
                exprStr);
        CompilationUnit unit = StaticJavaParser.parse(code);
        List<Node> nodeList = unit.stream(Node.TreeTraversal.PREORDER).toList();
        return nodeList.get(8);
    }

    private String determineVariableName(Node node){
        if (node instanceof Name) {
            return ((Name) node).asString();
        } else if (node instanceof SimpleName){
            return ((SimpleName) node).asString();
        } else if (node instanceof FieldAccessExpr){
            return node.toString();
        } else {
            throw new IllegalArgumentException("Node isn't a Name, SimpleName, or FieldAccessExpr");
        }
    }

    private String determineMethodCallName(Node node, VariableTypeMap typeMap){
        if (node instanceof MethodCallExpr){
            MethodCallExpr methodCallExpr = (MethodCallExpr) node;
            StringJoiner sj = new StringJoiner(", ", "(", ")");
            for (Expression expression : methodCallExpr.getArguments()){
                ParsedExpression currentExpr = new ParsedExpression(expression, typeMap);
                if (currentExpr.getType().isUndefined()){
                    return null;
                } else {
                    sj.add(currentExpr.getType().toString());
                }
            }
            Optional<Expression> scope = ((MethodCallExpr) node).getScope();
            String prefix = (scope.map(e -> e + ".").orElse("")) + ((MethodCallExpr) node).getName();
            return prefix + sj;
        } else {
            throw new IllegalArgumentException("Node isn't a MethodCallExpr");
        }
    }

    private String determineClassExprName(Node node){
        if (node instanceof ClassOrInterfaceType){
            ClassOrInterfaceType interfaceType = (ClassOrInterfaceType) node;
            return interfaceType.asString();
        } else if (node instanceof PrimitiveType){
            PrimitiveType primitiveType = (PrimitiveType) node;
            return primitiveType.asString();
        } else {
            throw new IllegalArgumentException("Node isn't a ClassOrInterfaceType or PrimitiveType");
        }
    }

    private boolean determineWhetherNodeIsOpenName(Node node){
        return (node instanceof Name ||
                node instanceof SimpleName ||
                node instanceof FieldAccessExpr ||
                node instanceof NameExpr ||
                node instanceof MethodCallExpr ||
                node instanceof ClassOrInterfaceType ||
                node instanceof PrimitiveType);
    }

    private boolean determineWhetherNodeIsRSHVariable(Node node){
        return (node instanceof Name ||
                node instanceof SimpleName ||
                node instanceof FieldAccessExpr) &&
                !(node.getParentNode().get() instanceof NameExpr &&
                        node.getParentNode().get().getParentNode().get() instanceof AssignExpr &&
                        node.getParentNode().get() == ((AssignExpr) node.getParentNode().get().getParentNode().get())
                                .getTarget()) &&
                !(node.getParentNode().get() instanceof VariableDeclarator &&
                        node == ((VariableDeclarator) node.getParentNode().get()).getName()) &&
                !(node.getParentNode().get() instanceof Parameter) &&
                !(node.getParentNode().get() instanceof FieldAccessExpr) &&
                !(node.getParentNode().get() instanceof NameExpr &&
                        node.getParentNode().get().getParentNode().get() instanceof FieldAccessExpr) &&
                !(node.getParentNode().get() instanceof Name) &&
                !(node.getParentNode().get() instanceof PackageDeclaration) &&
                !(node.getParentNode().get() instanceof ClassOrInterfaceDeclaration) &&
                !(node.getParentNode().get() instanceof ClassOrInterfaceType) &&
                !(node.getParentNode().get() instanceof MethodDeclaration) &&
                !(node.getParentNode().get() instanceof MethodCallExpr);
    }

    private boolean determineWhetherNodeIsMethodCall(Node node){
        return (node instanceof MethodCallExpr);
    }

    private boolean determineWhetherNodeIsClassExpr(Node node){
        return (node instanceof ClassOrInterfaceType) &&
                !(node.getParentNode().get() instanceof ClassOrInterfaceType) ||
                (node instanceof PrimitiveType);
    }

    private boolean determineWhetherNodeIsLHSVariable(Node node){
        return node instanceof SimpleName &&
                (node.getParentNode().isPresent() &&
                        node.getParentNode().get() instanceof NameExpr &&
                        node.getParentNode().get().getParentNode().isPresent() &&
                        node.getParentNode().get().getParentNode().get() instanceof AssignExpr &&
                        node.getParentNode().get() == ((AssignExpr) node.getParentNode().get().getParentNode().get())
                                .getTarget()) ||
                (node.getParentNode().isPresent() &&
                        node.getParentNode().get() instanceof VariableDeclarator &&
                        node == ((VariableDeclarator) node.getParentNode().get()).getName());
    }

    private ParsedType parseArithmeticExpr(Node node, VariableTypeMap typeMap, Map<Node, ParsedType> trustedNodesMap){
        ParsedType mostGeneralType = new ParsedType(JavaLiteralType.INTEGER);
        Deque<Node> nodeStack = new LinkedList<>();
        nodeStack.push(node);
        while (!nodeStack.isEmpty()){
            Node currentNode = nodeStack.pop();
            if (trustedNodesMap.containsKey(currentNode) && trustedNodesMap.get(currentNode).isNumericalType()){
                mostGeneralType = mostGeneralType.compareNumericalTypes(trustedNodesMap.get(currentNode));
            } else if (currentNode instanceof BinaryExpr){
                if (Set.of("+", "-", "*", "/").contains(((BinaryExpr) currentNode).getOperator().asString().trim())){
                    nodeStack.push(((BinaryExpr) currentNode).getLeft());
                    nodeStack.push(((BinaryExpr) currentNode).getRight());
                } else {
                    return null;
                }
            } else if (currentNode instanceof UnaryExpr) {
                if (Set.of("-").contains(((UnaryExpr) currentNode).getOperator().asString().trim())){
                    nodeStack.push(((UnaryExpr) currentNode).getExpression());
                } else {
                    return null;
                }
            } else if (currentNode instanceof MethodCallExpr){
                String method = determineMethodCallName(currentNode, typeMap);
                ParsedType parsedType = (method != null ? typeMap.getType(method, currentNode) : null);
                if (parsedType != null && parsedType.isNumericalType()){
                    mostGeneralType = mostGeneralType.compareNumericalTypes(parsedType);
                } else {
                    return null;
                }
            } else if (currentNode instanceof SimpleName){
                ParsedType parsedType = typeMap.getType(((SimpleName) currentNode).asString(), currentNode);
                if (parsedType != null && parsedType.isNumericalType()){
                    mostGeneralType = mostGeneralType.compareNumericalTypes(parsedType);
                } else {
                    return null;
                }
            } else if (currentNode instanceof FieldAccessExpr){
                ParsedType parsedType = typeMap.getType(currentNode.toString(), currentNode);
                if (parsedType != null && parsedType.isNumericalType()){
                    mostGeneralType = mostGeneralType.compareNumericalTypes(parsedType);
                } else {
                    return null;
                }
            } else if (currentNode instanceof NameExpr){
                nodeStack.push(((NameExpr) currentNode).getName());
            } else if (currentNode instanceof IntegerLiteralExpr){
                mostGeneralType = mostGeneralType.compareNumericalTypes(new ParsedType(JavaLiteralType.INTEGER));
            } else if (currentNode instanceof LongLiteralExpr){
                mostGeneralType = mostGeneralType.compareNumericalTypes(new ParsedType(JavaLiteralType.LONG));
            } else if (currentNode instanceof DoubleLiteralExpr){
                mostGeneralType = mostGeneralType.compareNumericalTypes(new ParsedType(JavaLiteralType.DOUBLE));
            } else if (currentNode instanceof EnclosedExpr){
                nodeStack.push(((EnclosedExpr) currentNode).getInner());
            } else {
                return null;
            }
        }
        return mostGeneralType;
    }

    private ParsedType parseBooleanExpr(Node node, VariableTypeMap typeMap, Map<Node, ParsedType> trustedNodesMap){
        Deque<Node> nodeStack = new LinkedList<>();
        nodeStack.push(node);
        while (!nodeStack.isEmpty()){
            Node currentNode = nodeStack.pop();
            if (trustedNodesMap.containsKey(currentNode) && trustedNodesMap.get(currentNode).isBooleanType()
                    || currentNode instanceof BooleanLiteralExpr){
                continue;
            } else if (currentNode instanceof BinaryExpr){
                if (((BinaryExpr) currentNode).getOperator().asString().trim().equals("==")){
                    continue;
                } else if (Set.of("&&", "||").contains(((BinaryExpr) currentNode).getOperator().asString().trim())){
                    nodeStack.push(((BinaryExpr) currentNode).getLeft());
                    nodeStack.push(((BinaryExpr) currentNode).getRight());
                } else {
                    return null;
                }
            } else if (currentNode instanceof UnaryExpr){
                if (((UnaryExpr) currentNode).getOperator().asString().trim().equals("!")){
                    nodeStack.push(((UnaryExpr) currentNode).getExpression());
                } else {
                    return null;
                }
            } else if (currentNode instanceof MethodCallExpr){
                String method = determineMethodCallName(currentNode, typeMap);
                ParsedType parsedType = (method != null ? typeMap.getType(method, currentNode) : null);
                if (parsedType == null || !parsedType.isBooleanType()){
                    return null;
                }
            } else if (currentNode instanceof FieldAccessExpr){
                ParsedType parsedType = typeMap.getType(currentNode.toString(), currentNode);
                if (parsedType == null || !parsedType.isBooleanType()){
                    return null;
                }
            } else if (currentNode instanceof SimpleName){
                ParsedType parsedType = typeMap.getType(((SimpleName) currentNode).asString(), currentNode);
                if (parsedType == null || !parsedType.isBooleanType()){
                    return null;
                }
            } else if (currentNode instanceof NameExpr){
                nodeStack.push(((NameExpr) currentNode).getName());
            } else if (currentNode instanceof EnclosedExpr){
                nodeStack.push(((EnclosedExpr) currentNode).getInner());
            } else {
                return null;
            }
        }
        return new ParsedType(JavaLiteralType.BOOLEAN);
    }

    public boolean isOpenName(){
        return isOpenName;
    }

    public boolean isRHSVariable(){
        return isRHSVariable;
    }

    public String getName(){
        return name;
    }

    public ParsedType getType(){
        return type;
    }

    public boolean isMethodCall() {
        return isMethodCall;
    }

    public boolean isClassExpression() {
        return isClassExpression;
    }

    public boolean isLHSVariable() {
        return isLHSVariable;
    }
}
