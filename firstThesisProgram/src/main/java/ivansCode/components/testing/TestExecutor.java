package ivansCode.components.testing;

import ivansCode.components.Project;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class TestExecutor {

    private final SummaryGeneratingListener listener = new SummaryGeneratingListener();
    private final Launcher launcher = LauncherFactory.create();
    private Project project;

    public TestResults execute(){
        if (project == null){
            throw new IllegalStateException("Project undefined, cannot execute tests");
        } else {
            String[] methodNames = project.getTestClassNames();
            TestResults testResults = new TestResults();
            for (String methodName : methodNames){
                LauncherDiscoveryRequestBuilder ldrb = LauncherDiscoveryRequestBuilder.request();
                ldrb.selectors(DiscoverySelectors.selectMethod(methodName));
                ldrb.build();
                LauncherDiscoveryRequest ldr = ldrb.build();
                try {
                    launcher.execute(launcher.discover(ldr), listener);
                    testResults.setPassed(methodName, (listener.getSummary().getTestsSucceededCount() >= 1));
                } catch (Exception e){
                    testResults.setPassed(methodName, false);
                }
            }
            return testResults;
        }
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
