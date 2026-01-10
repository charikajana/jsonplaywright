package com.framework.executor;

import com.framework.cucumber.UniversalStepDefinition;
import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.reporting.ErrorReporter;
import com.framework.strategy.SmartWaitStrategy;
import com.framework.utils.ParameterExtractor;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles navigation, windows, and system actions.
 */
public class NavigationHandler {
    private static final Logger logger = LoggerFactory.getLogger(NavigationHandler.class);

    public static boolean executeNavigate(Page page, ActionData action, String originalGherkinStep) {
        String url = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (url == null) url = action.getUrl();
        
        if (url == null) {
            logger.error("[ERROR] No URL for navigation");
            return false;
        }
        try {
            page.navigate(url);
            SmartWaitStrategy.waitForPageLoad(page);
            logger.info("[OK] Navigated to: {}", url);
            return true;
        } catch (Exception e) {
            ErrorReporter.reportStepError(originalGherkinStep, "NAVIGATE", "Failed to navigate to: " + url, e);
            return false;
        }
    }

    public static boolean executeSwitchWindow(Page page, ActionData action, String originalGherkinStep) {
        String windowId = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (windowId == null) windowId = action.getValue();
        
        try {
            BrowserContext context = page.context();
            java.util.List<Page> pages = context.pages();
            
            if ("new".equalsIgnoreCase(windowId) || "last".equalsIgnoreCase(windowId)) {
                Page lastPage = pages.get(pages.size() - 1);
                lastPage.bringToFront();
                logger.info("[OK] Switched to last/new window: {}", lastPage.url());
                return true;
            }
            
            for (Page p : pages) {
                if (p.url().contains(windowId) || p.title().contains(windowId)) {
                    p.bringToFront();
                    logger.info("[OK] Switched to window: {}", p.url());
                    return true;
                }
            }
            logger.warn("[WARN] Could not find window matching: {}", windowId);
            return false;
        } catch (Exception e) {
            logger.error("[ERROR] Switch window failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeScroll(Page page, ActionData action) {
        try {
            page.evaluate("window.scrollBy(0, 500)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean executeWaitNavigation(Page page, ActionData action) {
        try {
            page.waitForNavigation(() -> {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean executeScreenshot(Page page, ActionData action) {
        String baseDir = System.getProperty("screenshot.path", "reports/screenshots");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String fileName = "manual_screenshot_" + timestamp + ".png";
        
        try {
            Path path = Paths.get(baseDir);
            Files.createDirectories(path);
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Files.write(path.resolve(fileName), screenshot);
            logger.info("[INFO] Screenshot saved to: {}/{}", baseDir, fileName);
            
            Scenario scenario = UniversalStepDefinition.getCurrentScenario();
            if (scenario != null) {
                scenario.attach(screenshot, "image/png", "Manual Screenshot: " + action.getDescription());
                logger.debug("[OK] Attached screenshot to report");
            }
            return true;
        } catch (IOException e) {
            logger.error("[ERROR] Failed to save screenshot: {}", e.getMessage());
            return false;
        }
    }
}
