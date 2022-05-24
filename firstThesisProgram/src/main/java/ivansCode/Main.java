
package ivansCode;

import com.sun.tools.attach.VirtualMachine;
import ivansCode.techniques.ExampleClass;
import ivansCode.techniques.ExampleTechnique;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class Main {

    public static void main(String[] args) throws Exception {

        /*
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest ldr = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectPackage("exampleRootPackage"))
                .filters(includeClassNamePatterns(".*Test"))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(ldr);

        launcher.execute(testPlan, listener);
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(out));
         */

        // Path home = Path.of(System.getProperty("user.home") + "/Desktop");

        String javaSourceCodePathStr =
                "/Users/ivancharviakou/Desktop/bachelorThesisCode/" +
                        "firstThesisProgram/src/main/java/ivansCode/techniques/ExampleClass.java";
        Path javaSourceCodePath = Paths.get(javaSourceCodePathStr);


        ExampleTechnique exampleTechnique = new ExampleTechnique(ExampleClass.class, javaSourceCodePath);
        Class<?> smth = exampleTechnique.next();
        Object instance = smth.getDeclaredConstructor().newInstance();
        System.out.println(((ExampleClass) instance).add(4, 5));

    }

}
