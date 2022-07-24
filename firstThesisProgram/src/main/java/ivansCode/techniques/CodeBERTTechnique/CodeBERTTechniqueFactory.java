package ivansCode.techniques.CodeBERTTechnique;

import ivansCode.components.Project;
import ivansCode.components.techniques.TechniqueFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;

public class CodeBERTTechniqueFactory implements TechniqueFactory<CodeBERTTechnique> {

    private final List<Pair<Double, Double>> thresholds = List.of(
            Pair.of(0.0, 0.2),
            Pair.of(0.0, 0.4),
            Pair.of(0.0, 0.6),
            Pair.of(0.0, 0.8),
            Pair.of(0.0, 1.0),
            Pair.of(0.2, 1.0),
            Pair.of(0.4, 1.0),
            Pair.of(0.6, 1.0),
            Pair.of(0.8, 1.0)
    );

    @Override
    public int getNumConfigurations() {
        return thresholds.size() * 2;
    }

    @Override
    public CodeBERTTechnique getTechnique(int index, Project project) {
        Pair<Integer, Boolean> configuration = mapIndexToConfiguration(index);
        Pair<Double, Double> threshold = thresholds.get(configuration.getLeft());
        boolean useAllOperators = configuration.getRight();
        try {
            return new CodeBERTTechnique(threshold.getLeft(),
                    threshold.getRight(),
                    useAllOperators,
                    project.getOriginalSourceCode(),
                    project.getSubjectClass());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Couldn't create a technique", e);
        }
    }

    private Pair<Integer, Boolean> mapIndexToConfiguration(int index){
        if (index >= getNumConfigurations() || index < 0){
            throw new IllegalArgumentException(String.format("No such configuration, %d", index));
        } else {
            int thresholdIndex = index / 2;
            boolean allOperators = index % 2 == 0;
            return Pair.of(thresholdIndex, allOperators);
        }
    }

}
