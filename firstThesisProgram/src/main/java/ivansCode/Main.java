
package ivansCode;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class Main {

    public static void main(String[] args) throws Exception {

        Thread.currentThread().setContextClassLoader(new MemoryClassLoader());
        String targetName = ExampleClass.class.getName();
        IRuntime runtime = new LoggerRuntime();
        Instrumenter instrumenter = new Instrumenter(runtime);
        InputStream original = getTargetClass(targetName);
        byte[] instrumented = instrumenter.instrument(original, targetName);
        original.close();
        RuntimeData runtimeData = new RuntimeData();
        runtime.startup(runtimeData);
        MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
        memoryClassLoader.addDefinition(targetName, instrumented);
        Class<?> targetClass = memoryClassLoader.loadClass(targetName);

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
        summary.printTo(new PrintWriter(System.out));

        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        runtimeData.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        original = getTargetClass(targetName);
        analyzer.analyzeClass(original, targetName);
        original.close();

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            System.out.printf("Coverage of class %s%n", cc.getName());
            printCounter("instructions", cc.getInstructionCounter());
            printCounter("branches", cc.getBranchCounter());
            printCounter("lines", cc.getLineCounter());
            printCounter("methods", cc.getMethodCounter());
            printCounter("complexity", cc.getComplexityCounter());
        }

    }

    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }

    }

    private static InputStream getTargetClass(String targetName){
        String resource = '/' + targetName.replace('.', '/') + ".class";
        return Main.class.getResourceAsStream(resource);
    }

    private static void printCounter(final String unit, final ICounter counter) {
        final Integer missed = counter.getMissedCount();
        final Integer total = counter.getTotalCount();
        System.out.printf("%s of %s %s missed%n", missed, total, unit);
    }

}
