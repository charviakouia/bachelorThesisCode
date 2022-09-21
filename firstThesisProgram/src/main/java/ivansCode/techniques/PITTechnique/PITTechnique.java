package ivansCode.techniques.PITTechnique;

import com.github.javaparser.ParseProblemException;
import ivansCode.components.Mutant;
import ivansCode.components.techniques.Technique;
import ivansCode.techniques.PITTechnique.utils.FernFlowerExecutor;
import ivansCode.techniques.PITTechnique.utils.PITExecutor;
import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.IOUtility;
import org.jboss.windup.decompiler.fernflower.FernflowerDecompiler;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PITTechnique implements Technique {

    private final List<Mutant> mutants = new LinkedList<>();
    private final Iterator<Mutant> iterator;
    private final PITExecutor.ExecutionSetting setting;

    public PITTechnique(Path projectPath, Path targetCodePath, String originalSourceCode, String className,
                        String simpleClassName, PITExecutor.ExecutionSetting setting) throws IOException {
        IOUtility.saveTo(targetCodePath.getParent(), simpleClassName, ".java",
                originalSourceCode.getBytes(StandardCharsets.UTF_8));
        List<byte[]> bytes = PITExecutor.getByteCode(projectPath, ApplicationProperties.getTempPath(), className,
                targetCodePath, new String[]{}, setting);
        IOUtility.clearDirectory(ApplicationProperties.getTempPath());
        for (byte[] byteCode : bytes){
            try {
                String decompiledCode = FernFlowerExecutor.getDecompiledCode(byteCode, simpleClassName,
                        ApplicationProperties.getTempPath());
                Mutant currentMutant = new Mutant(className, byteCode, decompiledCode);
                this.mutants.add(currentMutant);
            } catch (ParseProblemException e){
                System.out.println("Couldn't decompile a PIT mutant");
            } finally {
                IOUtility.clearDirectory(ApplicationProperties.getTempPath());
            }
        }
        this.iterator = mutants.iterator();
        this.setting = setting;
    }

    @Override
    public String getDescription() {
        return String.format("__name_PIT__setting_%s__", setting.getCommandLineSetting());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Mutant next() {
        return iterator.next();
    }

}
