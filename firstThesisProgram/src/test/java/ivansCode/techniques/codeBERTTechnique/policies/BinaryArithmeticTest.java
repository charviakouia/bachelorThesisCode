package ivansCode.techniques.codeBERTTechnique.policies;

import com.google.common.collect.Lists;
import ivansCode.components.Mutant;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.CodeBERTTechnique;
import ivansCode.techniques.codeBERTTechnique.javaParserUtils.NodeUtils;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.CodeBERTOutputParser;
import ivansCode.utils.PythonExecutor;
import ivansCode.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BinaryArithmeticTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { int c = 5; Object a = c + 7; }";
        String originalClassName = "A";

        String maskedCode = "c <mask> 7";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, "+", "c + 7"),
                new CodeBERTOption(0.2, 33, "-", "c - 7"),
                new CodeBERTOption(0.2, 33, "*", "c * 7"),
                new CodeBERTOption(0.2, 33, "/", "c / 7"),
                new CodeBERTOption(0.2, 33, "&", "c & 7")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryArithmetic()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; Object a = c - 7; }",
                "class A { int c = 5; Object a = c * 7; }",
                "class A { int c = 5; Object a = c / 7; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testNestedExpression(){

        String code = "class A { int c() { return 5; } Object a = c() + 7 - 4; }";
        String originalClassName = "A";

        String maskedCode0 = "c() <mask> 7";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1.0, 33, "-", "c() - 7")
        ));

        String maskedCode1 = "c() + 7 <mask> 4";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, "-", "c() + 7 - 4"),
                new CodeBERTOption(0.2, 33, "*", "c() + 7 * 4"),
                new CodeBERTOption(0.2, 33, "/", "c() + 7 / 4"),
                new CodeBERTOption(0.4, 33, "&", "c() + 7 & 4")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryArithmetic()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c() { return 5; } Object a = c() - 7 - 4; }",
                "class A { int c() { return 5; } Object a = c() + 7 * 4; }",
                "class A { int c() { return 5; } Object a = c() + 7 / 4; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}