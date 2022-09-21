package ivansCode.techniques.PITTechnique;

import ivansCode.components.Project;
import ivansCode.components.techniques.TechniqueFactory;
import ivansCode.techniques.PITTechnique.utils.PITExecutor;

import java.io.IOException;

public class PITTechniqueFactory implements TechniqueFactory<PITTechnique> {

    @Override
    public int getNumConfigurations() {
        return PITExecutor.ExecutionSetting.values().length;
    }

    @Override
    public PITTechnique getTechnique(int index, Project project) {
        try {
            return new PITTechnique(project.getProjectPath(), project.getTargetCodePath(),
                    project.getOriginalSourceCode(), project.getSubjectClassName(), project.getSimpleSubjectClassName(),
                    PITExecutor.ExecutionSetting.values()[index]);
        } catch (IOException e){
            throw new IllegalStateException("Couldn't get the source code from the passed project", e);
        }
    }

}
