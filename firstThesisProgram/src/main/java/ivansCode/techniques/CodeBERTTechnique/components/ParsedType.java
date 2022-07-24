package ivansCode.techniques.CodeBERTTechnique.components;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.Type;
import ivansCode.utils.JavaLiteralType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ParsedType {

    private static final Set<String> numericalTypes = Set.of(
            "int", "Integer", "double", "Double", "float", "Float", "byte", "Byte", "short", "Short", "long", "Long"
    );

    private static final Set<String> booleanTypes = Set.of(
            "boolean", "Boolean"
    );

    private static final Set<String> bitwiseTypes = Set.of(
            "long", "Long", "int", "Integer", "short", "Short", "char", "Character", "byte", "Byte"
    );

    public static ParsedType getUndefined(){
        ParsedType result = new ParsedType();
        result.typeName = null;
        return result;
    }

    String typeName;

    private ParsedType(){}

    public ParsedType(Type type){
        typeName = type.asString().trim();
    }

    public ParsedType(LiteralExpr expr){
        JavaLiteralType javaLiteralType = getLiteralTypeFromLiteralExpression(expr);
        typeName = javaLiteralType.getStringValue().trim();
    }

    public ParsedType(String literalStr){
        String unlikelyClassName = "ABCABCJJIEJAQW";
        String unlikelyVariableName = "dkeewsdfbbsek";
        String code =
                String.format("class %s { Object %s = %s; }", unlikelyClassName, unlikelyVariableName, literalStr);
        CompilationUnit unit = StaticJavaParser.parse(code);
        List<Node> exprs = unit.stream(Node.TreeTraversal.PREORDER)
                .filter(node -> node instanceof LiteralExpr).collect(Collectors.toList());
        if (exprs.size() == 1){
            JavaLiteralType literalType = getLiteralTypeFromLiteralExpression((LiteralExpr) exprs.get(0));
            typeName = literalType.getStringValue().trim();
        } else {
            typeName = null;
        }
    }

    public ParsedType(JavaLiteralType literalType){
        typeName = literalType.getStringValue().trim();
    }

    private JavaLiteralType getLiteralTypeFromLiteralExpression(LiteralExpr expr){
        if (expr instanceof CharLiteralExpr){
            return JavaLiteralType.CHAR;
        } else if (expr instanceof StringLiteralExpr){
            return JavaLiteralType.STRING;
        } else if (expr instanceof IntegerLiteralExpr){
            return JavaLiteralType.INTEGER;
        } else if (expr instanceof LongLiteralExpr){
            return JavaLiteralType.LONG;
        } else if (expr instanceof NullLiteralExpr){
            return JavaLiteralType.NULL;
        } else if (expr instanceof BooleanLiteralExpr){
            return JavaLiteralType.BOOLEAN;
        } else if (expr instanceof DoubleLiteralExpr){
            return JavaLiteralType.DOUBLE;
        } else {
            throw new IllegalStateException("Unexpected literal expression detected");
        }
    }

    public boolean isNumericalType() {
        return !isUndefined() && numericalTypes.contains(typeName);
    }

    public boolean isBooleanType() {
        return !isUndefined() && booleanTypes.contains(typeName);
    }

    public boolean isBitwiseType() {
        return bitwiseTypes.contains(typeName);
    }

    public ParsedType compareNumericalTypes(ParsedType other){
        if (isNumericalType() && other.isNumericalType()){
            Set<String> types = new HashSet<>(List.of(typeName, other.typeName));
            if (types.contains("double") || types.contains("Double")){
                return new ParsedType(JavaLiteralType.DOUBLE);
            } else if (types.contains("long") || types.contains("Long")){
                return new ParsedType(JavaLiteralType.LONG);
            } else {
                return new ParsedType(JavaLiteralType.INTEGER);
            }
        } else {
            throw new IllegalArgumentException("Non-numerical type detected");
        }
    }

    public boolean isUndefined(){
        return typeName == null;
    }

    private String boxedToUnboxed(String boxed) {
        return switch (boxed) {
            case "Integer" -> "int";
            case "Float" -> "float";
            case "Double" -> "double";
            case "Byte" -> "byte";
            case "Short" -> "short";
            case "Character" -> "char";
            case "Boolean" -> "boolean";
            case "Long" -> "long";
            default -> boxed;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            ParsedType that = (ParsedType) o;
            return Objects.equals(boxedToUnboxed(this.typeName), boxedToUnboxed(that.typeName));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxedToUnboxed(typeName));
    }

    @Override
    public String toString(){
        return typeName;
    }

}
