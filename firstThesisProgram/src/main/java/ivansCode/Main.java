
package ivansCode;

import com.sun.tools.attach.VirtualMachine;
import exampleRootPackage.ExampleClass;
import exampleRootPackage.ExampleClassTest;
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
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class Main {

    public static void main(String[] args) throws Exception {

        IRuntime runtime = new LoggerRuntime();
        RuntimeData runtimeData = new RuntimeData();
        runtime.startup(runtimeData);

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

        runtime.shutdown();
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        runtimeData.collect(executionData, sessionInfos, false);

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        String className = ExampleClass.class.getName();
        InputStream original = getTargetClass(className);
        analyzer.analyzeClass(original, className);
        original.close();

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            out.printf("Coverage of class %s%n", cc.getName());
            printCounter("instructions", cc.getInstructionCounter());
            printCounter("branches", cc.getBranchCounter());
            printCounter("lines", cc.getLineCounter());
            printCounter("methods", cc.getMethodCounter());
            printCounter("complexity", cc.getComplexityCounter());
        }

    }

    private static InputStream getTargetClass(final String name) {
        final String resource = '/' + name.replace('.', '/') + ".class";
        return Main.class.getResourceAsStream(resource);
    }

    private static void printCounter(final String unit, final ICounter counter) {
        final Integer missed = counter.getMissedCount();
        final Integer total = counter.getTotalCount();
        System.out.printf("%s of %s %s missed%n", missed, total, unit);
    }

}
