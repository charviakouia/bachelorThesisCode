
package ivansCode;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class Main {

    public static void main(String[] args){

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest ldr = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage("exampleRootPackage"))
                .filters(includeClassNamePatterns(".*Test"))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(ldr);

        launcher.execute(testPlan, listener);
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));

    }

}
