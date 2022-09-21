package ivansCode.components.testing;

import ivansCode.components.Project;
import ivansCode.utils.ThreadService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestExecutor {

    /*
    private final SummaryGeneratingListener listener = new SummaryGeneratingListener();
    private final Launcher launcher = LauncherFactory.create();
    private Project project;
     */

    private final static Pattern d4jTestOutputFirstLinePattern =
            Pattern.compile("^Failing tests: (?<numFailingTests>0|[1-9][0-9]*)$");
    private final static Pattern d4jTestOutputDataLinePattern =
            Pattern.compile("^  - (?<packageName>(?:(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+\\.)*" +
                    "(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+)::" +
                    "(?<testName>(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+)(?:\\[(?<parameterName>.*)\\])?$");
    private Project project;

    public TestResults execute() throws IOException {

        if (project == null){

            throw new IllegalStateException("Project undefined, cannot execute tests");

        } else {

            Set<String> allTestMethodNames = project.getTestMethodNames();
            TestResults result = new TestResults();

            List<String> executionResult = executeD4jTests(project.getProjectPath());
            Set<String> failedClasses = getFailedClassesFromD4jOutput(executionResult, allTestMethodNames);

            for (String methodName : allTestMethodNames){
                result.setPassed(methodName, !failedClasses.contains(methodName));
            }

            return result;

            /*
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

             */
        }

    }

    private List<String> executeD4jTests(Path projectPath) throws IOException {

        Path pathToD4jBin = Path.of(
                Objects.requireNonNull(TestExecutor.class.getClassLoader().getResource("defects4j")).getPath(),
                "framework", "bin"
        );
        Path pathToJavaHome = Path.of(
                Objects.requireNonNull(TestExecutor.class.getClassLoader().getResource("jdk1.7.0_80.jdk"))
                        .getPath(),
                "Contents", "Home"
        );

        ProcessBuilder processBuilder = new ProcessBuilder(
                Path.of(pathToD4jBin.toString(), "defects4j").toString(), "test");
        processBuilder.directory(projectPath.toFile());
        Map<String, String> environment = processBuilder.environment();
        environment.put("JAVA_HOME", pathToJavaHome.toString());

        Process process = processBuilder.start();

        AtomicReference<List<String>> result = new AtomicReference<>();
        Future<?> future = ThreadService.submit(() -> {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                result.set(bf.lines().collect(Collectors.toList()));
                process.waitFor();
            } catch (IOException e){
                throw new IllegalStateException("Couldn't complete d4j execution", e);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });

        try {
            future.get(2, TimeUnit.MINUTES);
            return result.get();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Couldn't complete d4j execution", e);
        } catch (ExecutionException e){
            throw new IllegalStateException("Couldn't complete d4j execution", e);
        } catch (TimeoutException e){
            future.cancel(true);
            System.out.println("Timeout occurred while executing d4j test");
            return new LinkedList<>();
        } finally {
            process.destroyForcibly();
        }

    }

    private Set<String> getFailedClassesFromD4jOutput(List<String> d4jOutput, Set<String> allTests){

        if (d4jOutput == null || d4jOutput.isEmpty()){

            return new HashSet<>();

        } else {

            Iterator<String> iterator = d4jOutput.iterator();

            String firstLine = iterator.next();
            Matcher firstLineMatcher = d4jTestOutputFirstLinePattern.matcher(firstLine);
            int numFailingTests;
            if (firstLineMatcher.find()){
                numFailingTests = Integer.parseInt(firstLineMatcher.group("numFailingTests"));
            } else {
                throw new IllegalArgumentException("Cannot parse number of failing tests in output" + firstLine);
            }

            Set<String> failedClasses = new HashSet<>();
            while (iterator.hasNext()){
                String currentDataLine = iterator.next();
                Matcher dataLineMatcher = d4jTestOutputDataLinePattern.matcher(currentDataLine);
                if (dataLineMatcher.find()) {
                    String packageName = dataLineMatcher.group("packageName");
                    String testName = dataLineMatcher.group("testName");
                    String parameterName = dataLineMatcher.group("parameterName");
                    String formattedName;
                    if (parameterName != null){
                        formattedName = String.format("%s::%s[%s]", packageName, testName, parameterName);
                    } else {
                        formattedName = String.format("%s::%s", packageName, testName);
                    }
                    if (!allTests.contains(formattedName)){
                        throw new IllegalArgumentException("An unknown test has appeared: " + formattedName);
                    } else if (failedClasses.contains(formattedName)){
                        throw new IllegalArgumentException("A failed test has appeared twice: " + formattedName);
                    } else {
                        failedClasses.add(formattedName);
                    }
                } else {
                    throw new IllegalArgumentException("Cannot parse the name of a failing test: " + currentDataLine);
                }
            }

            if (failedClasses.size() == numFailingTests){
                return failedClasses;
            } else {
                throw new IllegalArgumentException("The reported number of failed tests is invalid");
            }

        }

    }

    public void setProject(Project project) {
        this.project = project;
    }

}
