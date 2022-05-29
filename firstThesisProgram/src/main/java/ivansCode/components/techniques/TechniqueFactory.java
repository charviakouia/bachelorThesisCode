package ivansCode.components.techniques;

import ivansCode.components.Project;

public interface TechniqueFactory<T extends Technique> {

    int getNumConfigurations();

    T getTechnique(int index, Project project);

}
