package runner;

import com.framework.utils.TestRunnerUtils;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.TestNG;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.framework.cucumber"},
        tags = "@UpdateTravelPolicy",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "json:target/cucumber.json",
                "html:target/cucumber-report.html"
        }
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {

    /**
     * This hook ensures that even when running via 'mvn test', 
     * the timestamped directories are created and Properties are set.
     */
    @BeforeSuite
    public void setupAllurePaths() {
        // Setup Dynamic Paths
        String basePath = TestRunnerUtils.getTimestampedPath("reports");
        String resultsPath = basePath + "/allure-results";
        String reportPath = basePath + "/allure-report";
        String screenshotsPath = basePath + "/screenshots";
        String videoPath = basePath + "/videos";

        // Set System Properties so the framework knows where to save files
        System.setProperty("allure.results.directory", resultsPath);
        System.setProperty("screenshot.path", screenshotsPath);
        System.setProperty("video.path", videoPath);
        
        // Save these for report generation in @AfterSuite
        System.setProperty("last.results.path", resultsPath);
        System.setProperty("last.report.path", reportPath);

        // Initialize Allure Environment File
        TestRunnerUtils.createAllureEnvironmentFile(resultsPath);
        
        System.out.println("=================================================");
        System.out.println("TEST EXECUTION STARTING");
        System.out.println("Results Path: " + resultsPath);
        System.out.println("=================================================");
    }

    /**
     * This hook generates the Allure report after all tests are finished.
     */
    @AfterSuite
    public void generateReport() {
        String resultsPath = System.getProperty("last.results.path");
        String reportPath = System.getProperty("last.report.path");
        
        if (resultsPath != null && reportPath != null) {
            TestRunnerUtils.generateAndOpenAllureReport(resultsPath, reportPath);
        }
    }

    /**
     * Keeps support for running as a standard Java application.
     */
    public static void main(String[] args) {
        TestNG testNG = new TestNG();
        XmlSuite suite = new XmlSuite();
        suite.setDataProviderThreadCount(TestRunnerUtils.getThreadCount());

        XmlTest test = new XmlTest(suite);
        test.setName("Cucumber Tests");
        test.setXmlClasses(Collections.singletonList(new XmlClass(RunCucumberTest.class)));

        testNG.setXmlSuites(Collections.singletonList(suite));
        testNG.setUseDefaultListeners(false);

        testNG.run();
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
