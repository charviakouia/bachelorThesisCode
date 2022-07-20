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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BinaryBitwiseTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { int c = 5; Object a = c & 7; }";
        String originalClassName = "A";

        String maskedCode = "c <mask> 7";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, "&", "c & 7"),
                new CodeBERTOption(0.2, 33, "|", "c | 7"),
                new CodeBERTOption(0.2, 33, ">>>", "c >>> 7"),
                new CodeBERTOption(0.2, 33, "q", "c q 7"),
                new CodeBERTOption(0.2, 33, "<<", "c << 7")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryBitwise()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; Object a = c | 7; }",
                "class A { int c = 5; Object a = c >>> 7; }",
                "class A { int c = 5; Object a = c << 7; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testNestedExpression(){

        String code = "class A { int c = 5; Object a = c & (3 | 9); }";
        String originalClassName = "A";

        String maskedCode0 = "c <mask> (3 | 9)";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1, 33, "<<", "c << (3 | 9)")
        ));

        String maskedCode1 = "3 <mask> 9";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, "&", "3 & 9"),
                new CodeBERTOption(0.2, 33, "|", "3 | 9"),
                new CodeBERTOption(0.3, 33, "q", "3 q 9"),
                new CodeBERTOption(0.3, 33, "<<", "3 << 9")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryBitwise()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; Object a = c << (3 | 9); }",
                "class A { int c = 5; Object a = c & (3 & 9); }",
                "class A { int c = 5; Object a = c & (3 << 9); }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}