package com.framework.executor;

import com.framework.cucumber.UniversalStepDefinition;
import com.framework.data.ActionData;
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
        String rawUrl = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (rawUrl == null) rawUrl = action.getUrl();
        
        String url = com.framework.utils.RandomDataResolver.resolve(rawUrl);
        
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
                com.framework.playwright.PlaywrightManager.getInstance().setPage(lastPage);
                logger.info("[OK] Switched to last/new window: {}", lastPage.url());
                return true;
            }
            
            // Switch to main window (first window) using index "0" or "main" or "first"
            if ("0".equals(windowId) || "main".equalsIgnoreCase(windowId) || "first".equalsIgnoreCase(windowId)) {
                if (!pages.isEmpty()) {
                    Page mainPage = pages.get(0);
                    mainPage.bringToFront();
                    com.framework.playwright.PlaywrightManager.getInstance().setPage(mainPage);
                    logger.info("[OK] Switched to main window: {}", mainPage.url());
                    return true;
                }
            }
            
            // Try to switch by index number
            try {
                int index = Integer.parseInt(windowId);
                if (index >= 0 && index < pages.size()) {
                    Page targetPage = pages.get(index);
                    targetPage.bringToFront();
                    com.framework.playwright.PlaywrightManager.getInstance().setPage(targetPage);
                    logger.info("[OK] Switched to window at index {}: {}", index, targetPage.url());
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // Not a number, try matching by URL/title
            }
            
            for (Page p : pages) {
                if (p.url().contains(windowId) || p.title().contains(windowId)) {
                    p.bringToFront();
                    com.framework.playwright.PlaywrightManager.getInstance().setPage(p);
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
    
    /**
     * Closes current window and switches to the specified window (main by default)
     */
    public static boolean executeCloseWindow(Page page, ActionData action) {
        try {
            BrowserContext context = page.context();
            java.util.List<Page> pages = context.pages();
            
            String targetWindow = action.getValue() != null ? action.getValue() : "0";
            
            // Close the current page
            page.close();
            logger.info("[OK] Closed current window");
            
            // Switch to target window
            pages = context.pages(); // Refresh page list after close
            if (pages.isEmpty()) {
                logger.warn("[WARN] No windows remaining after close");
                return false;
            }
            
            int targetIndex = 0;
            if ("main".equalsIgnoreCase(targetWindow) || "0".equals(targetWindow)) {
                targetIndex = 0;
            } else if ("last".equalsIgnoreCase(targetWindow)) {
                targetIndex = pages.size() - 1;
            } else {
                try {
                    targetIndex = Integer.parseInt(targetWindow);
                    if (targetIndex >= pages.size()) targetIndex = 0;
                } catch (NumberFormatException ignored) {
                    targetIndex = 0;
                }
            }
            
            Page targetPage = pages.get(targetIndex);
            targetPage.bringToFront();
            com.framework.playwright.PlaywrightManager.getInstance().setPage(targetPage);
            logger.info("[OK] Switched to window: {}", targetPage.url());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Close window failed: {}", e.getMessage());
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
    
    /**
     * Waits for page reload/refresh to complete.
     * This is useful after actions like dropdown selections that trigger page refresh.
     * The method first waits a short time for the reload to start, then waits for it to complete.
     */
    public static boolean executeWaitForReload(Page page, ActionData action) {
        try {
            String description = action.getDescription() != null ? action.getDescription() : "Wait for page reload";
            logger.info("[WAIT_RELOAD] {}", description);
            
            int timeout = 30000; // Default 30 second timeout
            String currentUrl = page.url();
            
            // Method 1: Wait for URL to change (covers both reload and navigation)
            // For reloads that don't change URL, we wait for network idle then stability
            try {
                // Small wait to let the page start its reload
                page.waitForTimeout(500);
                
                // Wait for network activity to complete (catches postback/reload)
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(timeout));
                
                logger.debug("[WAIT_RELOAD] Network idle achieved");
            } catch (Exception e) {
                logger.debug("[WAIT_RELOAD] Network idle check: {}", e.getMessage());
            }
            
            // Wait for page load state
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD,
                new Page.WaitForLoadStateOptions().setTimeout(timeout));
            
            // Wait for DOM content
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED,
                new Page.WaitForLoadStateOptions().setTimeout(timeout));
            
            // Additional wait for any JavaScript to settle
            page.waitForTimeout(500);
            
            // Additional stability check
            SmartWaitStrategy.waitForStable(page);
            
            String newUrl = page.url();
            if (!currentUrl.equals(newUrl)) {
                logger.info("[OK] Page navigated from {} to {}", currentUrl, newUrl);
            } else {
                logger.info("[OK] Page reload complete (same URL)");
            }
            return true;
        } catch (Exception e) {
            logger.warn("[WARN] Wait for reload completed with warning: {}", e.getMessage());
            return true; // Return true as page should be usable
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
    
    /**
     * Waits for page to be completely stable.
     * This includes: DOM loaded, Page load complete, Network idle, and JavaScript stability.
     */
    public static boolean executeWaitStable(Page page, ActionData action) {
        try {
            String description = action.getDescription() != null ? action.getDescription() : "Wait for page stability";
            logger.info("[WAIT_STABLE] {}", description);
            
            boolean result = SmartWaitStrategy.waitForStable(page);
            
            if (result) {
                logger.info("[OK] Page is stable and ready");
            }
            return result;
        } catch (Exception e) {
            logger.warn("[WARN] Wait stable completed with warning: {}", e.getMessage());
            return true; // Return true as page should be usable
        }
    }
}
