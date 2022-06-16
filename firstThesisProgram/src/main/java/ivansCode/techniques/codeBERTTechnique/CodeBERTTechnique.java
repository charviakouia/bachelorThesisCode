package ivansCode.techniques.codeBERTTechnique;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.google.common.collect.Lists;
import ivansCode.components.CodeBERTOption;
import ivansCode.components.Mutant;
import ivansCode.components.techniques.Technique;
import ivansCode.techniques.codeBERTTechnique.policies.ExamplePolicy;
import ivansCode.techniques.codeBERTTechnique.policies.Policy;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.CodeBERTOutputParser;
import ivansCode.utils.IOUtility;
import ivansCode.utils.PythonExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CodeBERTTechnique implements Technique, TokenParser {

    private final double thresholdFrom;
    private final double thresholdTo;
    private final boolean useAllOperators;
    private final CompilationUnit compilationUnit;
    private final Class<?> originalClass;

    private boolean cacheAvailable = false;
    private final List<Mutant> mutantCache = new ArrayList<>();
    private int currentCacheIndex = 0;
    private final Iterator<Node> nodeIterator;

    private final List<Policy> policies;

    public CodeBERTTechnique(double thresholdFrom, double thresholdTo, boolean useAllOperators, String sourceCode,
                             Class<?> originalClass) {
        this.thresholdFrom = thresholdFrom;
        this.thresholdTo = thresholdTo;
        this.useAllOperators = useAllOperators;
        this.originalClass = originalClass;
        this.compilationUnit = StaticJavaParser.parse(sourceCode);
        this.nodeIterator = compilationUnit.stream(Node.TreeTraversal.PREORDER).iterator();
        this.policies = Lists.newArrayList(new ExamplePolicy());
        if (useAllOperators){
            this.policies.addAll(List.of());
        }
        findNextMatch();
    }

    @Override
    public String getDescription() {
        return String.format(
                "{ thresholdFrom: %,.2f, thresholdTo: %,.2f, useAllOperators: %b }",
                thresholdFrom, thresholdTo, useAllOperators);
    }

    @Override
    public boolean hasNext() {
        return cacheAvailable;
    }

    @Override
    public Mutant next() {
        if (cacheAvailable){
            Mutant toReturn = mutantCache.get(currentCacheIndex++);
            if (currentCacheIndex == mutantCache.size()){
                findNextMatch();
                currentCacheIndex = 0;
            }
            return toReturn;
        } else {
            return null;
        }
    }

    private void findNextMatch(){
        cacheAvailable = false;
        mutantCache.clear();
        Set<Mutant> mutantSet = new TreeSet<>();
        while (!cacheAvailable && nodeIterator.hasNext()){
            Node currentNode = nodeIterator.next();
            for (Policy policy : policies){
                if (policy.isMatch(currentNode)){
                    fillMutantSet(currentNode, policy, mutantSet);
                }
            }
            cacheAvailable = !mutantSet.isEmpty();
        }
        mutantCache.addAll(mutantSet);
    }

    private void fillMutantSet(Node node, Policy policy, Set<Mutant> mutantSet){
        JavaToken firstToken = rewindToken(compilationUnit.getTokenRange().get().getBegin(), true);
        JavaToken lastToken = rewindToken(compilationUnit.getTokenRange().get().getEnd(), false);
        JavaToken leftOfMaskedToken = policy.getLeftOfMasked(node, this);
        JavaToken rightOfMaskedToken = policy.getRightOfMasked(node, this);
        int numTokensBeforeMask = getNumNodesInBetween(firstToken, leftOfMaskedToken);
        int numTokensAfterMask = getNumNodesInBetween(rightOfMaskedToken, lastToken);
        Pair<Integer, Integer> numCodeBERTTokens = policy.getDivision(numTokensBeforeMask, numTokensAfterMask);
        JavaToken leftCodeBERTToken = offsetToken(firstToken, numTokensBeforeMask - numCodeBERTTokens.getLeft());
        JavaToken rightCodeBERTToken = offsetToken(lastToken, numCodeBERTTokens.getRight() - numTokensAfterMask);
        String before = (numCodeBERTTokens.getLeft() == 0 ? "" : getStringBetweenNodes(leftCodeBERTToken, leftOfMaskedToken));
        String after = (numCodeBERTTokens.getRight() == 0 ? "" : getStringBetweenNodes(rightOfMaskedToken, rightCodeBERTToken));
        String between = policy.getCenterString(node);
        String prefix = getPrefix(firstToken, leftCodeBERTToken);
        String suffix = getSuffix(lastToken, rightCodeBERTToken);
        String maskedCode = (before + " <mask> " + after).replaceAll("\\s+", " ").trim();
        List<String> mutantCodes = runCodeBERT(maskedCode, between, policy);
        for (String mutantCode : mutantCodes){
            Mutant mutant = getMutant(prefix + " " + mutantCode + " " + suffix);
            if (mutant != null){
                mutantSet.add(mutant);
            }
        }
    }

    private String getPrefix(JavaToken first, JavaToken rightExcluded){
        JavaToken right = offsetToken(rightExcluded, -1);
        return (right == null ? "" : getStringBetweenNodes(first, right));
    }

    private String getSuffix(JavaToken last, JavaToken leftExcluded){
        JavaToken left = offsetToken(leftExcluded, 1);
        return (left == null ? "" : getStringBetweenNodes(left, last));
    }

    public JavaToken rewindToken(JavaToken token, boolean right){
        if (right){
            Optional<JavaToken> optionalNext = Optional.of(token);
            while (optionalNext.isPresent() && optionalNext.get().asString().isBlank()){
                optionalNext = optionalNext.get().getNextToken();
            }
            return optionalNext.orElse(null);
        } else {
            Optional<JavaToken> optionalPrevious = Optional.of(token);
            while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank()){
                optionalPrevious = optionalPrevious.get().getPreviousToken();
            }
            return optionalPrevious.orElse(null);
        }
    }

    private JavaToken offsetToken(JavaToken token, int offset){
        JavaToken current = null;
        if (offset < 0){
            Optional<JavaToken> optionalPrevious = Optional.of(token);
            while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank()){
                optionalPrevious = optionalPrevious.get().getPreviousToken();
            }
            boolean hasPrevious = optionalPrevious.isPresent();
            current = optionalPrevious.orElse(null);
            for (int i = 0; i < Math.abs(offset) && hasPrevious; i++){
                do {
                    optionalPrevious = optionalPrevious.get().getPreviousToken();
                } while (optionalPrevious.isPresent() && optionalPrevious.get().asString().isBlank());
                if (optionalPrevious.isEmpty()){
                    hasPrevious = false;
                } else {
                    current = optionalPrevious.get();
                }
            }
        } else {
            Optional<JavaToken> optionalNext = Optional.of(token);
            while (optionalNext.isPresent() && optionalNext.get().asString().isBlank()){
                optionalNext = optionalNext.get().getNextToken();
            }
            boolean hasNext = optionalNext.isPresent();
            current = optionalNext.orElse(null);
            for (int i = 0; i < offset && hasNext; i++){
                do {
                    optionalNext = optionalNext.get().getNextToken();
                } while (optionalNext.isPresent() && optionalNext.get().asString().isBlank());
                if (optionalNext.isEmpty()){
                    hasNext = false;
                } else {
                    current = optionalNext.get();
                }
            }
        }
        return current;
    }

    private int getNumNodesInBetween(JavaToken first, JavaToken last){
        if (first == last){
            return 0;
        } else {
            AtomicInteger numNodes = new AtomicInteger(0);
            new TokenRange(first, last).iterator().forEachRemaining(
                    token -> {
                        if (!token.asString().isBlank()){
                            numNodes.getAndIncrement();
                        }
                    }
            );
            return numNodes.get();
        }
    }

    private String getStringBetweenNodes(JavaToken first, JavaToken last){
        if (first == last){
            return "";
        } else {
            return new TokenRange(first, last).toString();
        }
    }

    private List<String> runCodeBERT(String maskedCode, String originalToken, Policy policy) {
        try {
            String pythonResult = PythonExecutor.executeScript(maskedCode);
            List<CodeBERTOption> options = CodeBERTOutputParser.parseCodeBERTStringOutput(pythonResult);
            List<String> mutantCodes = new LinkedList<>();
            filterDuplicates(options, originalToken);
            policy.filterOptions(options, originalToken);
            for (CodeBERTOption option : options){
                if (option.getScore() >= thresholdFrom && option.getScore() <= thresholdTo){
                    mutantCodes.add(option.getSequence());
                }
            }
            return mutantCodes;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Mutator cannot continue: Python script error", e);
        }
    }

    private void filterDuplicates(List<CodeBERTOption> options, String originalToken){
        double totalProbability = 0;
        double deletedProbability = 0;
        String originalTokenTrimmed = originalToken.trim();
        Map<String, CodeBERTOption> optionMap = new HashMap<>();
        for (CodeBERTOption option : options){
            totalProbability += option.getScore();
            String trimmed = option.getTokenString().trim();
            if (!originalTokenTrimmed.equals(trimmed)){
                if (!optionMap.containsKey(trimmed)){
                    optionMap.put(trimmed, option);
                } else {
                    CodeBERTOption existingOption = optionMap.get(trimmed);
                    existingOption.setScore(existingOption.getScore() + option.getScore());
                }
            } else {
                deletedProbability += option.getScore();
            }
        }
        for (CodeBERTOption option : optionMap.values()){
            option.setScore(option.getScore() / (1 - deletedProbability));
        }
        if (totalProbability > 1){
            throw new IllegalStateException("Assumption violated: Sum of probabilities is greater than 1");
        } else {
            options.retainAll(optionMap.values());
        }
    }

    private Mutant getMutant(String mutatedCode) {
        try {
            byte[] mutatedBytes = IOUtility.compileTo(
                    ApplicationProperties.getTempPath(),
                    originalClass.getSimpleName(),
                    mutatedCode, true);
            if (mutatedBytes == null){
                return null;
            } else {
                return new Mutant(originalClass, mutatedBytes, mutatedCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Mutator cannot continue: Compilation error", e);
        }
    }

}
