package ivansCode.techniques.codeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import ivansCode.components.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.TokenParser;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExamplePolicy implements Policy {

    private final static int INEXPLICABLE_DELTA = 6;
    private final static int TOKEN_LIMIT = 512 - INEXPLICABLE_DELTA;
    private final static int OPTIMISTIC_NUM_TOKENS_LEFT = TOKEN_LIMIT / 2;
    private final static int OPTIMISTIC_NUM_TOKENS_RIGHT = TOKEN_LIMIT - OPTIMISTIC_NUM_TOKENS_LEFT;

    @Override
    public boolean isMatch(Node node) {
        return node instanceof BinaryExpr;
    }

    @Override
    public JavaToken getLeftOfMasked(Node node, TokenParser parser) {
        return parser.rewindToken(((BinaryExpr) node).getLeft().getTokenRange().get().getEnd(), true);
    }

    @Override
    public JavaToken getRightOfMasked(Node node, TokenParser parser) {
        return parser.rewindToken(((BinaryExpr) node).getRight().getTokenRange().get().getBegin(), false);
    }

    @Override
    public String getCenterString(Node node) {
        return ((BinaryExpr) node).getOperator().asString();
    }

    @Override
    public Pair<Integer, Integer> getDivision(int numTokensLeft, int numTokensRight) {
        int leftDiff = numTokensLeft - OPTIMISTIC_NUM_TOKENS_LEFT;
        int rightDiff = numTokensRight - OPTIMISTIC_NUM_TOKENS_RIGHT;
        int left = Math.min(numTokensLeft, OPTIMISTIC_NUM_TOKENS_LEFT);
        int right = Math.min(numTokensRight, OPTIMISTIC_NUM_TOKENS_RIGHT);
        if (leftDiff < 0 && rightDiff > 0){
            right += Math.min(-leftDiff, rightDiff);
        }
        if (leftDiff > 0 && rightDiff < 0){
            left += Math.min(leftDiff, -rightDiff);
        }
        return Pair.of(left, right);
    }

    @Override
    public void filterOptions(List<CodeBERTOption> options, String originalToken) {
        Set<String> legalReplacements = Set.of("+", "-", "*", "/");
        List<CodeBERTOption> toRemove =
                options.stream().filter(option -> !legalReplacements.contains(option.getTokenString().trim()))
                        .collect(Collectors.toList());
        double deletedProbability = toRemove.stream().mapToDouble(CodeBERTOption::getScore).sum();
        options.removeAll(toRemove);
        for (CodeBERTOption option : options){
            option.setScore(option.getScore() / (1 - deletedProbability));
        }
    }

}
