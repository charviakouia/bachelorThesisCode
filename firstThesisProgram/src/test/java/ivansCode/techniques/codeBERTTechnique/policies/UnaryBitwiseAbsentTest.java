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

class UnaryBitwiseAbsentTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { int c = 5; void m() { int d = c; } }";
        String originalClassName = "A";

        String maskedCode = "<mask> c";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.2, 33, "~", "~c")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<PythonExecutor> mockedExecutor = Mockito.mockStatic(PythonExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new UnaryBitwiseAbsent()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; void m() { int d = (~c); } }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}