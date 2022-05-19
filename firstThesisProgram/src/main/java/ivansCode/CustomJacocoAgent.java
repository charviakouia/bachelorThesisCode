
package ivansCode;

import com.sun.tools.attach.VirtualMachine;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.lang.instrument.Instrumentation;

public class CustomJacocoAgent {

    public static void premain(String agentArgs, Instrumentation inst) {

    }

    public static void agentmain(String agentArgs, Instrumentation inst) {

        inst.addTransformer(new JacocoTransformer());

    }

}
