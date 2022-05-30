
package ivansCode;

import ivansCode.components.Mutant;
import ivansCode.components.techniques.Technique;
import ivansCode.components.techniques.TechniqueFactory;
import ivansCode.components.testing.KillMatrix;
import ivansCode.components.testing.TestResults;
import ivansCode.utils.ApplicationProperties;
import ivansCode.components.Project;
import ivansCode.components.testing.TestExecutor;

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
                    Technique technique = factory.getTechnique(i, project);
                    KillMatrix matrix = new KillMatrix();
                    while (technique.hasNext()){
                        Mutant mutant = technique.next();
                        mutant.install();
                        TestResults testResults = testExecutor.execute();
                        testResults.writeTo(matrix, mutant);
                    }
                    System.out.println(matrix);
                }
            }
        }

        // TODO: Write the quine-programs
        // TODO: Save the quine-programs and matrices
        // TODO: Write the PIT and CodeBERT techniques
        // TODO: Update the properties file

    }

}
