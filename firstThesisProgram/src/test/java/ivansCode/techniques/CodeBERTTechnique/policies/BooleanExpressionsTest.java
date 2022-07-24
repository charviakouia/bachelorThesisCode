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

class BooleanExpressionsTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { boolean c = true; Object a = c && (true || !c); int p = 6 + 7; }";
        String originalClassName = "A";

        String maskedCode = "<mask>";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "true", "true"),
                new CodeBERTOption(0.5, 33, "false", "false")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BooleanExpressions()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { boolean c = false; Object a = c && (true || !c); int p = 6 + 7; }",
                "class A { boolean c = true; Object a = true; int p = 6 + 7; }",
                "class A { boolean c = true; Object a = false; int p = 6 + 7; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testExpressionInMethod(){

        String code = "class A { void p() { boolean c = true; Object a = c && (true || !c); } }";
        String originalClassName = "A";

        String maskedCode = "<mask>";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "true", "true"),
                new CodeBERTOption(0.5, 33, "false", "false")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BooleanExpressions()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { void p() { boolean c = false; Object a = c && (true || !c); } }",
                "class A { void p() { boolean c = true; Object a = true; } }",
                "class A { void p() { boolean c = true; Object a = false; } }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}