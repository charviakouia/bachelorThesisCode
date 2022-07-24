package ivansCode.techniques.PITTechnique;

import ivansCode.components.Project;
import ivansCode.components.techniques.TechniqueFactory;

import java.io.IOException;

public class PITTechniqueFactory implements TechniqueFactory<PITTechnique> {

    @Override
    public int getNumConfigurations() {
        return 1;
    }

    @Override
    public PITTechnique getTechnique(int index, Project project) {
        try {
            return new PITTechnique(project.getSourceCodePath(), project.getSubjectClass());
        } catch (IOException e){
            throw new IllegalStateException("Couldn't get the source code from the passed project", e);
        }
    }

}
