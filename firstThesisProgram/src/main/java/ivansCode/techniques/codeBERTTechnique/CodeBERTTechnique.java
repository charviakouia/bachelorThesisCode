package ivansCode.techniques.codeBERTTechnique;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.google.common.collect.Lists;
import ivansCode.components.Mutant;
import ivansCode.components.techniques.Technique;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.CodeSplitter;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.VariableTypeMap;
import ivansCode.techniques.codeBERTTechnique.policies.*;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.CodeBERTOutputParser;
import ivansCode.utils.IOUtility;
import ivansCode.utils.PythonExecutor;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.*;

public class CodeBERTTechnique implements Technique {

    private final double thresholdFrom;
    private final double thresholdTo;
    private final boolean useAllOperators;
    private final CompilationUnit compilationUnit;
    private final Class<?> originalClass;
    private final String originalClassName;

    private boolean cacheAvailable = false;
    private final List<Mutant> mutantCache = new ArrayList<>();
    private int currentCacheIndex = 0;
    private final Iterator<Node> nodeIterator;

    private final List<Policy> policies;
    private final VariableTypeMap typeMap;

    public CodeBERTTechnique(double thresholdFrom, double thresholdTo, boolean useAllOperators, String sourceCode,
                             Class<?> originalClass) {
        this.thresholdFrom = thresholdFrom;
        this.thresholdTo = thresholdTo;
        this.useAllOperators = useAllOperators;
        this.originalClass = originalClass;
        this.originalClassName = originalClass.getSimpleName();
        this.compilationUnit = StaticJavaParser.parse(sourceCode);
        this.nodeIterator = compilationUnit.stream(Node.TreeTraversal.PREORDER).iterator();
        this.policies = Lists.newArrayList(
                new BinaryArithmetic(),
                new BinaryAssignment(),
                new BinaryBitwise(),
                new BinaryConditional(),
                new Constants(),
                new IncrementsAbsentLeft(),
                new IncrementsAbsentRight(),
                new IncrementsPresent(),
                new InvertNegativesAbsent(),
                new InvertNegativesPresent(),
                new UnaryBitwiseAbsent(),
                new UnaryBitwisePresent(),
                new UnaryConditionalAbsent(),
                new UnaryConditionalPresent()
        );
        if (useAllOperators) {
            this.policies.addAll(List.of(
                    new NumericalExpressions(),
                    new BooleanExpressions(),
                    new Variables(),
                    new MethodCalls(),
                    new ClassNames()
            ));
        }
        this.typeMap = new VariableTypeMap();
        findNextMatch();
    }

    public CodeBERTTechnique(String sourceCode, String originalClassName, List<Policy> policies){
        this.thresholdFrom = 0;
        this.thresholdTo = 1;
        this.useAllOperators = true;
        this.compilationUnit = StaticJavaParser.parse(sourceCode);
        this.originalClass = null;
        this.originalClassName = originalClassName;
        this.nodeIterator = compilationUnit.stream(Node.TreeTraversal.PREORDER).iterator();
        this.policies = new LinkedList<>(policies);
        this.typeMap = new VariableTypeMap();
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
        if (cacheAvailable) {
            Mutant toReturn = mutantCache.get(currentCacheIndex++);
            if (currentCacheIndex == mutantCache.size()) {
                findNextMatch();
                currentCacheIndex = 0;
            }
            return toReturn;
        } else {
            return null;
        }
    }

    private void findNextMatch() {
        cacheAvailable = false;
        mutantCache.clear();
        Set<Mutant> mutantSet = new TreeSet<>();
        while (!cacheAvailable && nodeIterator.hasNext()) {
            Node currentNode = nodeIterator.next();
            populateTypeMap(currentNode);
            for (Policy policy : policies) {
                if (policy.isMatch(currentNode, typeMap)) {
                    fillMutantSet(currentNode, policy, mutantSet);
                }
            }
            cacheAvailable = !mutantSet.isEmpty();
        }
        mutantCache.addAll(mutantSet);
    }

    private void populateTypeMap(Node node) {
        if (node instanceof NodeWithVariables) {
            for (VariableDeclarator variable : ((NodeWithVariables<?>) node).getVariables()) {
                typeMap.putVariable(variable);
            }
        } else if (node instanceof Parameter) {
            typeMap.putVariable((Parameter) node);
        } else if (node instanceof MethodDeclaration){
            typeMap.putVariable((MethodDeclaration) node);
        }
    }

    private void fillMutantSet(Node node, Policy policy, Set<Mutant> mutantSet) {

        CodeSplitter codeSplitter = new CodeSplitter(compilationUnit, node, policy);
        List<String> mutantCodes = runCodeBERT(policy, node, codeSplitter);
        for (String mutantCode : mutantCodes) {
            String completeCode = codeSplitter.getStringForSection(CodeSplitter.Section.PREFIX) +
                    " " + mutantCode + " " + codeSplitter.getStringForSection(CodeSplitter.Section.SUFFIX);
            Mutant mutant = getMutant(completeCode);
            if (mutant != null) {
                mutantSet.add(mutant);
            }
        }

    }

    private List<String> runCodeBERT(Policy policy, Node node, CodeSplitter splitter) {
        try {

            String pythonResult = PythonExecutor.executeScript(splitter.getMaskedCode());

            List<CodeBERTOption> options = CodeBERTOutputParser.parseCodeBERTStringOutput(pythonResult);
            double totalProbability = options.stream().mapToDouble(CodeBERTOption::getScore).sum();
            Assertions.assertTrue(totalProbability <= 1);

            filterDuplicates(options, policy.getCenterString(node));
            totalProbability = options.stream().mapToDouble(CodeBERTOption::getScore).sum();
            Assertions.assertTrue(totalProbability <= 1);

            policy.filterOptions(options, node, typeMap);
            totalProbability = options.stream().mapToDouble(CodeBERTOption::getScore).sum();
            Assertions.assertTrue(totalProbability <= 1);

            policy.addOptions(options, splitter.getLeftCodeBERTSequence(), splitter.getRightCodeBERTSequence());
            totalProbability = options.stream().mapToDouble(CodeBERTOption::getScore).sum();
            Assertions.assertTrue(totalProbability <= 1);

            policy.correctOptions(options,
                    splitter.getStringForSection(CodeSplitter.Section.BEFORE_NODE),
                    splitter.getStringForSection(CodeSplitter.Section.BEFORE_MASKED),
                    splitter.getStringForSection(CodeSplitter.Section.AFTER_MASKED),
                    splitter.getStringForSection(CodeSplitter.Section.AFTER_NODE));

            List<String> mutantCodes = new LinkedList<>();
            for (CodeBERTOption option : options) {
                if (option.getScore() >= thresholdFrom && option.getScore() <= thresholdTo) {
                    mutantCodes.add(option.getSequence());
                }
            }
            return mutantCodes;

        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Mutator cannot continue: Python script error", e);
        }
    }

    private void filterDuplicates(List<CodeBERTOption> options, String originalToken) {
        double deletedProbability = 0;
        String originalTokenTrimmed = originalToken.trim();
        Map<String, CodeBERTOption> optionMap = new HashMap<>();
        for (CodeBERTOption option : options) {
            String trimmed = option.getTokenString().trim();
            if (!originalTokenTrimmed.equals(trimmed)) {
                if (!optionMap.containsKey(trimmed)) {
                    optionMap.put(trimmed, option);
                } else {
                    CodeBERTOption existingOption = optionMap.get(trimmed);
                    existingOption.setScore(existingOption.getScore() + option.getScore());
                }
            } else {
                deletedProbability += option.getScore();
            }
        }
        if (deletedProbability != 1){
            for (CodeBERTOption option : optionMap.values()) {
                option.setScore(option.getScore() / (1 - deletedProbability));
            }
        } else {
            options.clear();
        }
        options.retainAll(optionMap.values());
    }

    private Mutant getMutant(String mutatedCode) {
        try {
            byte[] mutatedBytes = IOUtility.compileTo(
                    ApplicationProperties.getTempPath(),
                    originalClassName,
                    mutatedCode, true);
            if (mutatedBytes == null) {
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
