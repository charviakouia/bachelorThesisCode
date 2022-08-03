package ivansCode.techniques.PITTechnique;

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
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PITTechnique implements Technique {

    private final List<Mutant> mutants = new LinkedList<>();
    private final Iterator<Mutant> iterator;

    public PITTechnique(Path sourceCodePath, Class<?> clazz) throws IOException {
        List<byte[]> bytes =
                PITExecutor.getByteCode(ApplicationProperties.getTempPath(), clazz, sourceCodePath, new String[]{});
        IOUtility.clearDirectory(ApplicationProperties.getTempPath());
        for (byte[] byteCode : bytes){
            String decompiledCode =
                    FernFlowerExecutor.getDecompiledCode(byteCode, clazz, ApplicationProperties.getTempPath());
            IOUtility.clearDirectory(ApplicationProperties.getTempPath());
            Mutant currentMutant = new Mutant(clazz, byteCode, decompiledCode);
            mutants.add(currentMutant);
        }
        iterator = mutants.iterator();
    }

    @Override
    public String getDescription() {
        return "__name_PIT__";
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
