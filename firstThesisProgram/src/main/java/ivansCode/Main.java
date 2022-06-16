
package ivansCode;

import ivansCode.components.Mutant;
import ivansCode.components.QuineBuilder;
import ivansCode.components.techniques.Technique;
import ivansCode.components.techniques.TechniqueFactory;
import ivansCode.components.testing.KillMatrix;
import ivansCode.components.testing.TestResults;
import ivansCode.utils.ApplicationProperties;
import ivansCode.components.Project;
import ivansCode.components.testing.TestExecutor;
import ivansCode.utils.IOUtility;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        ApplicationProperties.readApplicationProperties();

        List<Project> projects = ApplicationProperties.getProjects();
        List<TechniqueFactory<? extends Technique>> techniqueFactories = ApplicationProperties.getFactories();
        TestExecutor testExecutor = new TestExecutor();

        for (Project project : projects){
            testExecutor.setProject(project);
            for (TechniqueFactory<? extends Technique> factory : techniqueFactories){
                int numConfigurations = factory.getNumConfigurations();
                for (int i = 0; i < numConfigurations; i++){
                    QuineBuilder quineBuilder = new QuineBuilder(project.getOriginalSourceCode());
                    KillMatrix matrix = new KillMatrix();
                    Technique technique = factory.getTechnique(i, project);
                    while (technique.hasNext()){
                        Mutant mutant = technique.next();
                        if (mutant.install()){
                            TestResults testResults = testExecutor.execute();
                            testResults.writeTo(matrix, mutant);
                            quineBuilder.add(mutant);
                        }
                    }
                    Path savePath = Path.of(ApplicationProperties.getOutputPath().toString(),
                            project.getSubjectClass().getName(), technique.getDescription());
                    IOUtility.saveTo(savePath, KillMatrix.FILE_NAME, KillMatrix.FILE_TYPE,
                            matrix.toString().getBytes(StandardCharsets.UTF_8) , true);
                    IOUtility.compileTo(savePath, QuineBuilder.CLASS_NAME, quineBuilder.build(), true);
                }
            }
        }
        // TODO: Complete the properties file
    }

}
