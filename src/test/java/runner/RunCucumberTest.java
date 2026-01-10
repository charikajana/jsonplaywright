package runner;

import com.framework.utils.TestRunnerUtils;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.framework.cucumber",
        tags = "@login",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber.json",
                "html:target/cucumber-report.html"
        }
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {

    public static void main(String[] args) {
        // Setup Dynamic Paths
        String basePath = TestRunnerUtils.getTimestampedPath("reports");
        String resultsPath = basePath + "/allure-results";
        String reportPath = basePath + "/allure-report";
        String screenshotsPath = basePath + "/screenshots";
        String videoPath = basePath + "/videos";

        System.setProperty("allure.results.directory", resultsPath);
        System.setProperty("screenshot.path", screenshotsPath);
        System.setProperty("video.path", videoPath);

        // Initialize Environment File
        TestRunnerUtils.createAllureEnvironmentFile(resultsPath);

        // Configure TestNG
        TestNG testNG = new TestNG();
        XmlSuite suite = new XmlSuite();
        suite.setDataProviderThreadCount(TestRunnerUtils.getThreadCount());

        XmlTest test = new XmlTest(suite);
        test.setName("Cucumber Tests");
        test.setXmlClasses(Collections.singletonList(new XmlClass(RunCucumberTest.class)));

        testNG.setXmlSuites(Collections.singletonList(suite));
        testNG.setUseDefaultListeners(false);

        // Run Execution
        testNG.run();

        // Finalize Reporting
        TestRunnerUtils.generateAndOpenAllureReport(resultsPath, reportPath);
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
