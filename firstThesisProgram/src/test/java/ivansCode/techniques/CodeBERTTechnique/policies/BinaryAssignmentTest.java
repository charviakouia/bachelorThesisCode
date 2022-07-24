package ivansCode.techniques.CodeBERTTechnique.policies;

import com.google.common.collect.Lists;
import ivansCode.components.Mutant;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.CodeBERTTechnique;
import ivansCode.utils.ApplicationProperties;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTOutputParser;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTExecutor;
import ivansCode.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;

class BinaryAssignmentTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { int c = 5; void m() { c += 2; } }";
        String originalClassName = "A";

        String maskedCode = "c <mask> 2";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, ">>>=", "c >>>= 2"),
                new CodeBERTOption(0.2, 33, "%=", "c %= 2"),
                new CodeBERTOption(0.3, 33, "+=", "c += 2"),
                new CodeBERTOption(0.3, 33, "=", "c = 2")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryAssignment()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; void m() { c >>>= 2; } }",
                "class A { int c = 5; void m() { c %= 2; } }",
                "class A { int c = 5; void m() { c = 2; } }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testRHSNesting(){

        String code = "class A { int c = 5; void m() { c += (c += 3); } }";
        String originalClassName = "A";

        String maskedCode0 = "c <mask> (c += 3)";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1, 33, ">>>=", "c >>>= (c += 3)")
        ));

        String maskedCode1 = "c <mask> 3";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1, 33, "%=", "c %= 3")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryAssignment()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; void m() { c >>>= (c += 3); } }",
                "class A { int c = 5; void m() { c += (c %= 3); } }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}