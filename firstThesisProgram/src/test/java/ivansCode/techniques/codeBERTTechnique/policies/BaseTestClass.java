package ivansCode.techniques.codeBERTTechnique.policies;

import ivansCode.techniques.codeBERTTechnique.CodeBERTOption;
import ivansCode.utils.CodeBERTOutputParser;
import ivansCode.utils.PythonExecutor;
import org.mockito.MockedStatic;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;

public class BaseTestClass {

    protected void mapCodeToOptions(String maskedCode, List<CodeBERTOption> options,
                                  MockedStatic<PythonExecutor> mockedExecutor,
                                  MockedStatic<CodeBERTOutputParser> mockedParser){
        String hashStr = String.valueOf(Objects.hash(maskedCode));
        mockedExecutor.when(() -> PythonExecutor.executeScript(eq(maskedCode)))
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
