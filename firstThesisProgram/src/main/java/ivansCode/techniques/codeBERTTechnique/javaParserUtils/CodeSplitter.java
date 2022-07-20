package ivansCode.techniques.codeBERTTechnique.javaParserUtils;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import ivansCode.techniques.codeBERTTechnique.policies.Policy;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeSplitter {

    private final JavaToken[] leftTokens;
    private final JavaToken[] rightTokens;

    public enum Section {

        PREFIX(0),
        BEFORE_NODE(1),
        BEFORE_MASKED(2),
        AFTER_MASKED(3),
        AFTER_NODE(4),
        SUFFIX(5);

        private final int value;
        private static final Map<Integer, Section> sectionMap;
        static {
            sectionMap = new HashMap<>();
            for (Section section : Section.values()){
                sectionMap.put(section.getValue(), section);
            }
        }

        Section(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Section getSectionForValue(int value){
            return sectionMap.get(value);
        }

    }

    public CodeSplitter(Node root, Node node, Policy policy){
        leftTokens = new JavaToken[Section.values().length];
        rightTokens = new JavaToken[Section.values().length];
        List<Integer> numCodeBERTTokens = new ArrayList<>();
        List<JavaToken> tokensAroundMasked = new ArrayList<>();
        calculateTokensAroundMasked(root, node, policy, numCodeBERTTokens, tokensAroundMasked);
        calculateTokensAroundNode(node, policy, tokensAroundMasked);
        calculateTokensAroundEdges(numCodeBERTTokens, tokensAroundMasked);
        calculateTokensAroundEnds(root, tokensAroundMasked);
    }

    private void calculateTokensAroundMasked(Node root, Node node, Policy policy,
                                             List<Integer> numCodeBERTTokens,
                                             List<JavaToken> tokensAroundMasked){

        JavaToken firstToken = NodeUtils.nextNonBlankToken(root.getTokenRange().get().getBegin(), true);
        JavaToken leftOfMasked = policy.getLeftOfMasked(node);
        int numTokensBeforeMask = 0;
        if (firstToken != null && leftOfMasked != null && firstToken.getPreviousToken().isPresent() &&
                NodeUtils.nextNonBlankToken(firstToken.getPreviousToken().get(), false) != leftOfMasked){
            numTokensBeforeMask = numNonBlankTokensBetween(firstToken, leftOfMasked);
        }

        JavaToken rightOfMasked = policy.getRightOfMasked(node);
        JavaToken lastToken = NodeUtils.nextNonBlankToken(root.getTokenRange().get().getEnd(), false);
        int numTokensAfterMask = 0;
        if (rightOfMasked != null && lastToken != null && lastToken.getNextToken().isPresent() &&
                NodeUtils.nextNonBlankToken(lastToken.getNextToken().get(), true) != rightOfMasked){
            numTokensAfterMask = numNonBlankTokensBetween(rightOfMasked, lastToken);
        }

        Pair<Integer, Integer> divisionPair = policy.getDivision(numTokensBeforeMask, numTokensAfterMask);
        numCodeBERTTokens.add(0, divisionPair.getLeft());
        numCodeBERTTokens.add(1, divisionPair.getRight());
        tokensAroundMasked.add(0, leftOfMasked);
        tokensAroundMasked.add(1, rightOfMasked);

    }

    private void calculateTokensAroundNode(Node node, Policy policy, List<JavaToken> tokensAroundMasked){

        Node trueNode = policy.getTrueNode(node);

        JavaToken firstOfNode = NodeUtils.nextNonBlankToken(trueNode.getTokenRange().get().getBegin(), true);
        JavaToken leftOfMasked = tokensAroundMasked.get(0);
        if (firstOfNode != null && leftOfMasked != null && firstOfNode.getPreviousToken().isPresent() &&
                NodeUtils.nextNonBlankToken(firstOfNode.getPreviousToken().get(), false) != leftOfMasked){
            leftTokens[Section.BEFORE_MASKED.value] = firstOfNode;
            rightTokens[Section.BEFORE_MASKED.value] = leftOfMasked;
        }

        JavaToken rightOfMasked = tokensAroundMasked.get(1);
        JavaToken lastOfNode = NodeUtils.nextNonBlankToken(trueNode.getTokenRange().get().getEnd(), false);
        if (rightOfMasked != null && lastOfNode != null && lastOfNode.getNextToken().isPresent() &&
                NodeUtils.nextNonBlankToken(lastOfNode.getNextToken().get(), true) != rightOfMasked){
            leftTokens[Section.AFTER_MASKED.value] = rightOfMasked;
            rightTokens[Section.AFTER_MASKED.value] = lastOfNode;
        }

    }

    private void calculateTokensAroundEdges(List<Integer> numCodeBERTTokens, List<JavaToken> tokensAroundMasked){

        JavaToken leftOfMasked = tokensAroundMasked.get(0);
        JavaToken firstOfEdge = null;
        JavaToken leftOfNode = null;
        JavaToken firstOfNode = leftTokens[Section.BEFORE_MASKED.value];
        int numTokensLeft = numCodeBERTTokens.get(0);
        if (leftOfMasked != null && numTokensLeft > 0){
            firstOfEdge = offsetToken(leftOfMasked, 1 - numTokensLeft);
        }
        if (firstOfEdge != null && firstOfEdge != firstOfNode){
            leftOfNode = (firstOfNode != null ?
                    NodeUtils.nextNonBlankToken(firstOfNode.getPreviousToken().get(), false) : leftOfMasked);
            leftTokens[Section.BEFORE_NODE.value] = firstOfEdge;
            rightTokens[Section.BEFORE_NODE.value] = leftOfNode;
        }

        JavaToken rightOfMasked = tokensAroundMasked.get(1);
        JavaToken lastOfEdge = null;
        JavaToken rightOfNode = null;
        JavaToken lastOfNode = rightTokens[Section.AFTER_MASKED.value];
        int numTokensRight = numCodeBERTTokens.get(0);
        if (rightOfMasked != null && numTokensRight > 0){
            lastOfEdge = offsetToken(rightOfMasked, numTokensRight - 1);
        }
        if (lastOfEdge != null && lastOfEdge != lastOfNode){
            rightOfNode = (lastOfNode != null ?
                    NodeUtils.nextNonBlankToken(lastOfNode.getNextToken().get(), true) : rightOfMasked);
            leftTokens[Section.AFTER_NODE.value] = rightOfNode;
            rightTokens[Section.AFTER_NODE.value] = lastOfNode;
        }

    }

    private void calculateTokensAroundEnds(Node root, List<JavaToken> tokensAroundMasked){

        JavaToken firstToken = NodeUtils.nextNonBlankToken(root.getTokenRange().get().getBegin(), true);
        JavaToken leftOfEdge = null;
        JavaToken firstOfEdge = null;
        if (leftTokens[Section.BEFORE_NODE.value] != null){
            firstOfEdge = leftTokens[Section.BEFORE_NODE.value];
            if (firstOfEdge.getPreviousToken().isPresent()){
                leftOfEdge = NodeUtils.nextNonBlankToken(firstOfEdge.getPreviousToken().get(), false);
            }
        } else if (leftTokens[Section.BEFORE_MASKED.value] != null){
            firstOfEdge = leftTokens[Section.BEFORE_MASKED.value];
            if (firstOfEdge.getPreviousToken().isPresent()){
                leftOfEdge = NodeUtils.nextNonBlankToken(firstOfEdge.getPreviousToken().get(), false);
            }
        } else if (tokensAroundMasked.get(0) != null){
            leftOfEdge = tokensAroundMasked.get(0);
        }
        if (firstToken != null && leftOfEdge != null){
            leftTokens[Section.PREFIX.value] = firstToken;
            rightTokens[Section.PREFIX.value] = leftOfEdge;
        }

        JavaToken lastToken = NodeUtils.nextNonBlankToken(root.getTokenRange().get().getEnd(), false);
        JavaToken rightOfEdge = null;
        JavaToken lastOfEdge = null;
        if (rightTokens[Section.AFTER_NODE.value] != null){
            lastOfEdge = rightTokens[Section.AFTER_NODE.value];
            if (lastOfEdge.getNextToken().isPresent()){
                rightOfEdge = NodeUtils.nextNonBlankToken(lastOfEdge.getNextToken().get(), true);
            }
        } else if (rightTokens[Section.AFTER_MASKED.value] != null){
            lastOfEdge = rightTokens[Section.AFTER_MASKED.value];
            if (lastOfEdge.getNextToken().isPresent()){
                rightOfEdge = NodeUtils.nextNonBlankToken(lastOfEdge.getNextToken().get(), true);
            }
        } else if (tokensAroundMasked.get(1) != null){
            rightOfEdge = tokensAroundMasked.get(1);
        }
        if (lastToken != null && rightOfEdge != null){
            leftTokens[Section.SUFFIX.value] = rightOfEdge;
            rightTokens[Section.SUFFIX.value] = lastToken;
        }

    }

    private JavaToken offsetToken(JavaToken token, int offset) {
        JavaToken current = null;
        if (offset < 0) {
            Optional<JavaToken> optionalPrevious = Optional.of(token);
            while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank()) {
                optionalPrevious = optionalPrevious.get().getPreviousToken();
            }
            boolean hasPrevious = optionalPrevious.isPresent();
            current = optionalPrevious.orElse(null);
            for (int i = 0; i < Math.abs(offset) && hasPrevious; i++) {
                do {
                    optionalPrevious = optionalPrevious.get().getPreviousToken();
                } while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank());
                if (optionalPrevious.isEmpty()) {
                    hasPrevious = false;
                } else {
                    current = optionalPrevious.get();
                }
            }
        } else {
            Optional<JavaToken> optionalNext = Optional.of(token);
            while (optionalNext.isPresent() && optionalNext.get().asString().isBlank()) {
                optionalNext = optionalNext.get().getNextToken();
            }
            boolean hasNext = optionalNext.isPresent();
            current = optionalNext.orElse(null);
            for (int i = 0; i < offset && hasNext; i++) {
                do {
                    optionalNext = optionalNext.get().getNextToken();
                } while (optionalNext.isPresent() && optionalNext.get().asString().isBlank());
                if (optionalNext.isEmpty()) {
                    hasNext = false;
                } else {
                    current = optionalNext.get();
                }
            }
        }
        return current;
    }

    public static int numNonBlankTokensBetween(JavaToken first, JavaToken last) {
        AtomicInteger numNodes = new AtomicInteger(0);
        new TokenRange(first, last).iterator().forEachRemaining(
                token -> {
                    if (!token.asString().isBlank()) {
                        numNodes.getAndIncrement();
                    }
                }
        );
        return numNodes.get();
    }

    public String getStringForSection(Section section){
        JavaToken left = leftTokens[section.value];
        JavaToken right = rightTokens[section.value];
        if (left != null && right != null){
            return new TokenRange(left, right).toString();
        } else {
            return "";
        }
    }

    public String getMaskedCode(){
        return (getLeftCodeBERTSequence() + " <mask> " + getRightCodeBERTSequence())
                .replaceAll("\\s+", " ").trim();
    }

    public String getLeftCodeBERTSequence(){
        return getStringForSection(Section.BEFORE_NODE) + getStringForSection(Section.BEFORE_MASKED);
    }

    public String getRightCodeBERTSequence(){
        return getStringForSection(Section.AFTER_MASKED) + getStringForSection(Section.AFTER_NODE);
    }

}
