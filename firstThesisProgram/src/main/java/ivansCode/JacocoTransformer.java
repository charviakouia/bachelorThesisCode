

package ivansCode;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class JacocoTransformer implements ClassFileTransformer {

    private final Instrumenter instrumenter;

    public JacocoTransformer(){
        instrumenter = new Instrumenter(new LoggerRuntime());
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer)
            throws IllegalClassFormatException {

        try {
            return instrumenter.instrument(classfileBuffer, className);
        } catch (IOException e){
            throw new IllegalClassFormatException(e.getMessage());
        }

    }

}
