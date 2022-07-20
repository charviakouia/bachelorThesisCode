package ivansCode.techniques.codeBERTTechnique.javaParserUtils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableTypeMap {

    private final Map<String, ParsedType> variableToType = new TreeMap<>();

    public ParsedType getType(String varName, Node startingNode) {
        String qualifiedName = getQualifiedName(startingNode.getParentNode().orElse(null), "");
        String reversedQualifiedName = new StringBuilder(qualifiedName).reverse().toString();
        String currentPrefix = null;
        ParsedType result = null;
        Matcher matcher = Pattern.compile("(\\.|#)").matcher(reversedQualifiedName);
        while (result == null && matcher.find()) {
            currentPrefix = new StringBuilder(reversedQualifiedName.substring(matcher.start())).reverse() + varName;
            result = variableToType.get(currentPrefix);
        }
        return result;
    }

    public void putVariable(VariableDeclarator variable) {
        String variableName =
                getQualifiedName(variable.getParentNode().orElse(null), variable.getNameAsString());
        ParsedType type = new ParsedType(variable.getType());
        putVariable(variableName, type);
    }

    public void putVariable(Parameter parameter) {
        String variableName =
                getQualifiedName(parameter.getParentNode().orElse(null), parameter.getNameAsString());
        ParsedType type = new ParsedType(parameter.getType());
        putVariable(variableName, type);
    }

    public void putVariable(MethodDeclaration methodDeclaration){
        String methodName =
                getQualifiedName(methodDeclaration.getParentNode().orElse(null),
                        methodDeclaration.getSignature().asString());
        ParsedType type = new ParsedType(methodDeclaration.getType());
        putVariable(methodName, type);
    }

    public void putVariable(String varName, ParsedType type) {
        variableToType.put(varName, type);
    }

    private String getQualifiedName(Node node, String initialName) {
        StringBuilder sb = new StringBuilder();
        sb.insert(0, initialName);
        Node currentParent = node;
        while (currentParent != null) {
            if (currentParent instanceof ClassOrInterfaceDeclaration) {
                sb.insert(0, ".");
                sb.insert(0, ((ClassOrInterfaceDeclaration) currentParent).getName());
            } else if (currentParent instanceof MethodDeclaration) {
                sb.insert(0, "#");
                sb.insert(0, ((MethodDeclaration) currentParent).getSignature().asString());
            } else if (currentParent instanceof NodeWithBody) {
                sb.insert(0, "#");
                sb.insert(0, String.format("<%d>", currentParent.hashCode()));
                sb.insert(0, currentParent.getMetaModel().getTypeNameGenerified());
            }
            currentParent = currentParent.getParentNode().orElse(null);
        }
        return sb.toString();
    }

    public void clear() {
        variableToType.clear();
    }

}
