package ivansCode.techniques.CodeBERTTechnique.policies;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.NodeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CommonPolicy implements Policy {

    private final static int INEXPLICABLE_DELTA = 6;
    private final static int TOKEN_LIMIT = 512 - INEXPLICABLE_DELTA;
    private final static int OPTIMISTIC_NUM_TOKENS_LEFT = TOKEN_LIMIT / 2;
    private final static int OPTIMISTIC_NUM_TOKENS_RIGHT = TOKEN_LIMIT - OPTIMISTIC_NUM_TOKENS_LEFT;

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
    public void correctOptions(List<CodeBERTOption> options, String beforeNode, String beforeMasked,
                               String afterMasked, String afterNode){}

    @Override
    public void addOptions(List<CodeBERTOption> options, String beforeSequence, String afterSequence) {}

    @Override
    public Node getTrueNode(Node node){
        return node;
    }

    protected JavaToken getLeftOfTokenRange(TokenRange range) {
        Optional<JavaToken> previousToken = range.getBegin().getPreviousToken();
        return previousToken.map(token -> NodeUtils.nextNonBlankToken(token, false)).orElse(null);
    }

    protected JavaToken getFirstOfTokenRange(TokenRange range){
        JavaToken firstToken = range.getBegin();
        if (firstToken != null){
            return NodeUtils.nextNonBlankToken(firstToken, true);
        } else {
            return null;
        }
    }

    protected JavaToken getRightOfTokenRange(TokenRange range) {
        Optional<JavaToken> nextToken = range.getEnd().getNextToken();
        return nextToken.map(token -> NodeUtils.nextNonBlankToken(token, true)).orElse(null);
    }

    protected JavaToken getLastOfTokenRange(TokenRange range){
        JavaToken lastToken = range.getEnd();
        if (lastToken != null){
            return NodeUtils.nextNonBlankToken(lastToken, false);
        } else {
            return null;
        }
    }

    protected void filterOptionsByInclusionInSet(List<CodeBERTOption> options, Set<String> legalOptions){
        List<CodeBERTOption> toRemove =
                options.stream().filter(option -> !legalOptions.contains(option.getTokenString().trim()))
                        .collect(Collectors.toList());
        filterOptionsByRemoveSet(options, toRemove);
    }

    protected void filterOptionsByRemoveSet(List<CodeBERTOption> options, List<CodeBERTOption> toRemove){
        double deletedProbability = toRemove.stream().mapToDouble(CodeBERTOption::getScore).sum();
        options.removeAll(toRemove);
        if (deletedProbability != 1){
            for (CodeBERTOption option : options){
                option.setScore(option.getScore() / (1 - deletedProbability));
            }
        } else {
            options.clear();
        }
    }

    protected void includeOption(List<CodeBERTOption> options, String sequence, String token, double probability){
        CodeBERTOption toAdd = new CodeBERTOption(probability, 33, token, sequence);
        if (probability != 1){
            for (CodeBERTOption option : options){
                option.setScore(option.getScore() * (1 - probability));
            }
        } else {
            options.clear();
        }
        options.add(toAdd);
    }

}
