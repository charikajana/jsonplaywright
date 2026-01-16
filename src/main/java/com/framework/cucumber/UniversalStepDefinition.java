package com.framework.cucumber;

import com.framework.executor.JsonEnhancedExecutor;
import com.framework.playwright.PlaywrightManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Link;
import com.microsoft.playwright.Page;
import com.framework.strategy.SmartWaitStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Universal Step Definition - handles ALL Gherkin steps
 * Uses JSON data from Playwright agent execution for precise element location
 */
public class UniversalStepDefinition {
    private static final Logger logger = LoggerFactory.getLogger(UniversalStepDefinition.class);
    
    private JsonEnhancedExecutor jsonExecutor;
    private static PlaywrightManager playwrightManager;
    private String currentFeatureName;
    private boolean isFirstStep = true;
    private static final ThreadLocal<Scenario> scenarioThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> allureUuidThreadLocal = new ThreadLocal<>();
    
    public static Scenario getCurrentScenario() {
        return scenarioThreadLocal.get();
    }
    
    @Before
    public void setUp(Scenario scenario) {
        com.framework.utils.RandomDataResolver.clearCache();
        scenarioThreadLocal.set(scenario);
        logger.info("[INFO] [Thread: {}] Scenario starting: {}", 
            Thread.currentThread().getName(), scenario.getName());
        
        // Extract feature name from scenario
        String uri = scenario.getUri().toString();
        currentFeatureName = extractFeatureName(uri);
        logger.info("[INFO] [Thread: {}] Feature: {}", 
            Thread.currentThread().getName(), currentFeatureName);
        
        // Capture Allure UUID
        Allure.getLifecycle().getCurrentTestCase().ifPresent(allureUuidThreadLocal::set);
        
        // Get singleton PlaywrightManager instance
        playwrightManager = PlaywrightManager.getInstance();
        
        // Initialize browser for THIS thread (creates thread-local context and page)
        playwrightManager.initializeBrowser();
        logger.info("[INFO] [Thread: {}] Browser initialized for this thread", 
            Thread.currentThread().getName());
        
        // Initialize JSON-enhanced executor for this scenario
        jsonExecutor = new JsonEnhancedExecutor(currentFeatureName);
    }
    
    private void addMetadataFromTags(Scenario scenario) {
        if (scenario == null) return;
        Collection<String> tags = scenario.getSourceTagNames();
        
        for (String tag : tags) {
            String cleanTag = tag.startsWith("@") ? tag.substring(1) : tag;
            // Custom types not handled natively by the Allure-Cucumber plugin
            if (cleanTag.startsWith("rally=") || cleanTag.startsWith("RallyID_")) {
                String id = cleanTag.contains("=") ? cleanTag.split("=")[1] : cleanTag.substring(8);
                // The URL is built using the pattern defined in allure.properties if we use the 3-arg version
                // but Allure.link doesn't automatically resolve pattern for 3-arg from properties in all versions.
                // We'll use the 2-arg version for simplicity or build the URL manually.
                Allure.link("Rally: " + id, "https://rally1.rallydev.com/#/search?keywords=" + id);
            }
        }
    }
    
    @After
    public void tearDown(Scenario scenario) {
        addMetadataFromTags(scenario);
        
        if (scenario.isFailed()) {
            logger.info("[DEBUG] Scenario FAILED: {}", scenario.getName());
            if (playwrightManager != null) {
                try {
                    logger.info("[DEBUG] Attempting failure screenshot...");
                    byte[] screenshot = playwrightManager.takeScreenshot();
                    if (screenshot != null && screenshot.length > 0) {
                        // Attach to Cucumber/Allure report
                        scenario.attach(screenshot, "image/png", "Failure Screenshot");
                        logger.info("[DEBUG] Failure screenshot attached. Size: {}", screenshot.length);
                        
                        // Save to local file system
                        saveScreenshotToFile(scenario.getName() + "_failure", screenshot);
                    } else {
                        logger.error("[DEBUG] Screenshot was null or empty!");
                    }
                } catch (Exception e) {
                    logger.error("[DEBUG] Failed to capture failure screenshot: {}", e.getMessage());
                }
            } else {
                logger.error("[DEBUG] PlaywrightManager is null, cannot take screenshot!");
            }
        }
        
        logger.info("[INFO] [Thread: {}] Scenario complete: {}", 
            Thread.currentThread().getName(), scenario.getName());
        
        // Capture video path before closing browser
        java.nio.file.Path videoPath = null;
        if (playwrightManager != null) {
            videoPath = playwrightManager.getVideoPath();
            logger.info("[INFO] [Thread: {}] Video record found at: {}", 
                Thread.currentThread().getName(), videoPath);
        }

        // Close browser context for THIS thread only
        if (playwrightManager != null) {
            playwrightManager.closeBrowser();
            logger.info("[INFO] [Thread: {}] Browser context closed for this thread", 
                Thread.currentThread().getName());
        }

        // Attach video to Allure report if it exists
        if (videoPath != null && Files.exists(videoPath)) {
            try {
                byte[] videoBytes = Files.readAllBytes(videoPath);
                String videoFileName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".webm";
                scenario.attach(videoBytes, "video/webm", "Execution Video: " + videoFileName);
                logger.info("[OK] [Thread: {}] Video attached to Allure: {}", 
                    Thread.currentThread().getName(), videoFileName);
            } catch (IOException e) {
                logger.error("[ERROR] Failed to attach video: {}", e.getMessage());
            }
        }
        
        // Clear thread-local executor (but NOT the shared playwrightManager)
        jsonExecutor = null;
        
        // Clean up thread-local variables
        allureUuidThreadLocal.remove();
        scenarioThreadLocal.remove();
    }
    
    private void saveScreenshotToFile(String scenarioName, byte[] screenshot) {
        String baseDir = System.getProperty("screenshot.path", "reports/screenshots");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String fileName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".png";
        
        try {
            Path path = Paths.get(baseDir);
            Files.createDirectories(path);
            Files.write(path.resolve(fileName), screenshot);
            logger.info("[INFO] Screenshot saved to: {}/{}", baseDir, fileName);
        } catch (IOException e) {
            logger.error("Failed to save screenshot: {}", e.getMessage());
        }
    }
    
    /**
     * Universal step definition - catches all steps
     * First tries JSON-enhanced execution, falls back to original if needed
     */
    @Given("^(.*)$")
    public void executeStep(String step) {
        logger.info("[INFO] Step: {}", step);
        
        // Ensure browser is fully loaded and stable before executing ANY step
        Page page = playwrightManager.getPage();
        if (page != null) {
            SmartWaitStrategy.waitForPageLoad(page);
            SmartWaitStrategy.waitForNetworkIdle(page);
        }
        
        // Execute through JSON-enhanced executor
        // It will automatically check cache/steps/ and fallback to standard execution if needed
        boolean success = jsonExecutor.executeStep(step);
        
        if (success) {
            logger.info("[SUCCESS] Step passed");
        } else {
            logger.error("[ERROR] Step failed");
            throw new AssertionError("Step failed: " + step);
        }
    }
    
    /**
     * Extract feature name from URI
     */
    private String extractFeatureName(String uri) {
        // Extract filename from path like "file:///path/to/Login.feature"
        String[] parts = uri.split("/");
        String filename = parts[parts.length - 1];
        return filename; // Returns "Login.feature"
    }
}

