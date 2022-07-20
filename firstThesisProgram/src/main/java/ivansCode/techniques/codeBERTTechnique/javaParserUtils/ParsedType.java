package ivansCode.techniques.codeBERTTechnique.javaParserUtils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.Type;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
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

    public static ParsedType getUndefined(){
        ParsedType result = new ParsedType();
        result.typeName = null;
        return result;
    }

    private boolean boxedEquals(String a, String b){
        Set<String> integers = Set.of("int", "Integer");
        Set<String> floats = Set.of("float", "Float");
        Set<String> doubles = Set.of("double", "Double");
        Set<String> bytes = Set.of("byte", "Byte");
        Set<String> shorts = Set.of("short", "Short");
        Set<String> characters = Set.of("char", "Character");
        Set<String> booleans = Set.of("boolean", "Boolean");
        Set<String> longs = Set.of("long", "Long");
        return integers.contains(a) && integers.contains(b) ||
                floats.contains(a) && floats.contains(b) ||
                doubles.contains(a) && doubles.contains(b) ||
                bytes.contains(a) && bytes.contains(b) ||
                shorts.contains(a) && shorts.contains(b) ||
                characters.contains(a) && characters.contains(b) ||
                booleans.contains(a) && booleans.contains(b) ||
                longs.contains(a) && longs.contains(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            ParsedType that = (ParsedType) o;
            return Objects.equals(typeName, that.typeName) ||
                    boxedEquals(typeName, that.typeName);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName);
    }

    @Override
    public String toString(){
        return typeName;
    }

}
