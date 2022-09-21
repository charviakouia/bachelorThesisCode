package ivansCode.techniques.CodeBERTTechnique.policies;

import com.google.common.collect.Lists;
import ivansCode.components.Mutant;
import ivansCode.techniques.CodeBERTTechnique.CodeBERTTechnique;
import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTExecutor;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTOutputParser;
import ivansCode.utils.ApplicationProperties;
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

class BinaryRelationalTest extends BaseTestClass {

    @BeforeAll
    static void init() throws IOException {
        ApplicationProperties.readApplicationProperties();
    }

    @Test
    void testSimpleExpression(){

        String code = "class A { int c = 5; Object a = c < 6; }";
        String originalClassName = "A";

        String maskedCode = "c <mask> 6";
        List<CodeBERTOption> options = Lists.newLinkedList(List.of(
                new CodeBERTOption(0.5, 33, "<", "c < 6"),
                new CodeBERTOption(0.5, 33, ">=", "c >= 6")
        ));

        Set<String> calculatedResults = new HashSet<>();
        try (
                MockedStatic<CodeBERTExecutor> mockedExecutor = Mockito.mockStatic(CodeBERTExecutor.class);
                MockedStatic<CodeBERTOutputParser> mockedParser = Mockito.mockStatic(CodeBERTOutputParser.class)
        ) {
            mapCodeToOptions(maskedCode, options, mockedExecutor, mockedParser);
            CodeBERTTechnique technique = new CodeBERTTechnique(code, originalClassName,
                    List.of(new BinaryRelational()));
            while (technique.hasNext()){
                Mutant mutant = technique.next();
                calculatedResults.add(StringUtils.normalizeWhitespace(mutant.toString()));
            }
        }

        Set<String> expectedResults = Set.of(
                "class A { int c = 5; Object a = c >= 6; }"
        );
        Assertions.assertEquals(expectedResults, calculatedResults);

    }

}