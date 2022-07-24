package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;

public class IncrementsAbsentRight extends IncrementsAbsent {

    @Override
    public JavaToken getLeftOfMasked(Node node) {
        return getLastOfTokenRange(node.getTokenRange().get());
    }

    @Override
    public JavaToken getRightOfMasked(Node node) {
        return getRightOfTokenRange(node.getTokenRange().get());
    }

}
