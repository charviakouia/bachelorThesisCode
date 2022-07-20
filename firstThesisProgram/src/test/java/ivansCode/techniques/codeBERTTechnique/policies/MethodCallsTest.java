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

class MethodCallsTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleMethodCall(){

        String code = "class A { int p(){ return 5; } int m(){ return 5; } int a = m(); }";
        String originalClassName = "A";

        String maskedCode = "<mask>";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "m()", "m()"),
                new CodeBERTOption(0.5, 33, "p()", "p()")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new MethodCalls()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int p() { return 5; } int m() { return 5; } int a = p(); }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testMethodCallWithFieldAccess(){

        String code = "class A { void m(){ System.out.println(); } }";
        String originalClassName = "A";

        String maskedCode0 = "System.out. <mask>";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "printf(\"\")",
                        "System.out.printf(\"\")")
        ));

        String maskedCode1 = "System. <mask>";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "currentTimeMillis()",
                        "System.currentTimeMillis()")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new MethodCalls()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { void m() { System.out.printf(\"\"); } }",
                "class A { void m() { System.currentTimeMillis(); } }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}