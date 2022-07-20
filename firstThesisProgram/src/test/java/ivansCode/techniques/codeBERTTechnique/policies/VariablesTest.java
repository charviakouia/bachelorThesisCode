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

class VariablesTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleVariable(){

        String code = "class A { int c = 2; int a = 5; int b = a; }";
        String originalClassName = "A";

        String maskedCode = "<mask>";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "c", "c"),
                new CodeBERTOption(0.5, 33, "7", "7")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new Variables()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 2; int a = 5; int b = c; }",
                "class A { int c = 2; int a = 5; int b = 7; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testMethodCallWithFieldAccess(){

        String code = "class A { class B { public static int b = 3; public static int c = 3; } int a = B.b; }";
        String originalClassName = "A";

        String maskedCode0 = "B. <mask>";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "c",
                        "B.c")
        ));

        String maskedCode1 = "<mask>";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "7",
                        "7")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new Variables()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { class B { public static int b = 3; public static int c = 3; } int a = B.c; }",
                "class A { class B { public static int b = 3; public static int c = 3; } int a = 7; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}