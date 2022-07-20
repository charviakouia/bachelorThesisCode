package ivansCode.techniques.codeBERTTechnique.policies;

import com.google.common.collect.Lists;
import ivansCode.components.Mutant;
import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.techniques.codeBERTTechnique.CodeBERTTechnique;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.CodeBERTOutputParser;
import ivansCode.utils.PythonExecutor;
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

import static org.junit.jupiter.api.Assertions.*;

class BinaryConditionalTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { boolean c = true; Object a = c && false; }";
        String originalClassName = "A";

        String maskedCode = "c <mask> false";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "&&", "c && false"),
                new CodeBERTOption(0.5, 33, "||", "c || false")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryConditional()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { boolean c = true; Object a = c || false; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testNestedExpression(){

        String code = "class A { boolean c = true; Object a = (true || false) && c; }";
        String originalClassName = "A";

        String maskedCode0 = "(true || false) <mask> c";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "||", "(true || false) || c"),
                new CodeBERTOption(0.5, 33, "&&", "(true || false) && c")
        ));

        String maskedCode1 = "true <mask> false";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1, 33, "&&", "true && false")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryConditional()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { boolean c = true; Object a = (true || false) || c; }",
                "class A { boolean c = true; Object a = (true && false) && c; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}