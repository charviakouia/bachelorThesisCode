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

class ClassNamesTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleCast(){

        String code = "class A { double a = (double) 5 + 7.0; }";
        String originalClassName = "A";

        String maskedCode = "<mask>";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "double", "double"),
                new CodeBERTOption(0.5, 33, "Integer", "Integer")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new ClassNames()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { double a = (Integer) 5 + 7.0; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

    @Test
    void testCastWithFieldAccess(){

        String code = "class A { double a = (java.lang.Double) 5 + 7.0; }";
        String originalClassName = "A";

        String maskedCode0 = "<mask>";
        List<CodeBERTOption> options0 = Lists.newLinkedList(List.of(
                new CodeBERTOption(1, 33, "double", "double")
        ));

        String maskedCode1 = "java.lang. <mask>";
        List<CodeBERTOption> options1 = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.3, 33, "Integer", "java.lang.Integer"),
                new CodeBERTOption(0.4, 33, "Double", "java.lang.Double")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode0, options0, mockedExecutor, mockedParser);
            mapCodeToOptions(maskedCode1, options1, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new ClassNames()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { double a = (double) 5 + 7.0; }",
                "class A { double a = (java.lang.Integer) 5 + 7.0; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}