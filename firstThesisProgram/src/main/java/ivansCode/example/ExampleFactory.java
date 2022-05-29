package ivansCode.example;

import ivansCode.components.techniques.TechniqueFactory;
import ivansCode.utils.ApplicationProperties;
import ivansCode.components.Project;

public final class ExampleFactory implements TechniqueFactory<ExampleTechnique> {

    @Override
    public int getNumConfigurations() {
        return 1;
    }

    @Override
    public ExampleTechnique getTechnique(int index, Project project) {
        try {
            return new ExampleTechnique(
                    Class.forName(project.getSubjectClassName()),
                    project.getSourceCodePath(),
                    ApplicationProperties.getTempPath());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
