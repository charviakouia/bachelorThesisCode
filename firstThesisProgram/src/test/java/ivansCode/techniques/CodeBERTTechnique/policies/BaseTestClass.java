package ivansCode.techniques.CodeBERTTechnique.policies;

import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTOutputParser;
import ivansCode.techniques.CodeBERTTechnique.utils.CodeBERTExecutor;
import org.mockito.MockedStatic;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;

public class BaseTestClass {

    protected void mapCodeToOptions(String maskedCode, List<CodeBERTOption> options,
                                  MockedStatic<CodeBERTExecutor> mockedExecutor,
                                  MockedStatic<CodeBERTOutputParser> mockedParser){
        String hashStr = String.valueOf(Objects.hash(maskedCode));
        mockedExecutor.when(() -> CodeBERTExecutor.executeScript(eq(maskedCode)))
                .thenReturn(hashStr);
        mockedParser.when(() -> CodeBERTOutputParser.parseCodeBERTStringOutput(hashStr))
                .thenAnswer(invocationOnMock -> {
                    List<CodeBERTOption> listOfClones = new LinkedList<>();
                    for (CodeBERTOption option : options){
                        listOfClones.add(option.clone());
                    }
                    return listOfClones;
                });
    }

}
