package com.framework.strategy;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart waiting strategy with dynamic polling and adaptive timeouts.
 * Optimizes test execution speed while maintaining reliability.
 */
public class SmartWaitStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SmartWaitStrategy.class);
    
    // Default timeout configurations (in milliseconds)
    private static final int DEFAULT_TIMEOUT = 10000;
    private static final int SHORT_TIMEOUT = 3000;
    private static final int MEDIUM_TIMEOUT = 7000;
    private static final int LONG_TIMEOUT = 15000;
    
    // Polling interval
    private static final int POLL_INTERVAL = 100;
    
    /**
     * Waits for an element to be visible with adaptive timeout.
     * 
     * @param locator The Playwright locator
     * @param actionType The type of action (affects timeout duration)
     * @return true if element became visible, false otherwise
     */
    public static boolean waitForVisible(Locator locator, String actionType) {
        int timeout = getTimeoutForAction(actionType);
        
        try {
            long startTime = System.currentTimeMillis();
            
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeout));
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("[WAIT] Element visible after {}ms (timeout: {}ms)", duration, timeout);
            
            return true;
        } catch (Exception e) {
            logger.warn("[WAIT] Element not visible within {}ms", timeout);
            return false;
        }
    }
    
    /**
     * Waits for an element to be attached to DOM.
     * 
     * @param locator The Playwright locator
     * @return true if element is attached, false otherwise
     */
    public static boolean waitForAttached(Locator locator) {
        try {
            long startTime = System.currentTimeMillis();
            
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(SHORT_TIMEOUT));
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("[WAIT] Element attached after {}ms", duration);
            
            return true;
        } catch (Exception e) {
            logger.warn("[WAIT] Element not attached within {}ms", SHORT_TIMEOUT);
            return false;
        }
    }
    
    /**
     * Waits for an element to be enabled (interactive).
     * 
     * @param locator The Playwright locator
     * @return true if element is enabled, false otherwise
     */
    public static boolean waitForEnabled(Locator locator) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Wait for element to be visible first
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(MEDIUM_TIMEOUT));
            
            // Then check if it's enabled
            int attempts = 0;
            int maxAttempts = MEDIUM_TIMEOUT / POLL_INTERVAL;
            
            while (attempts < maxAttempts) {
                if (locator.isEnabled()) {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("[WAIT] Element enabled after {}ms", duration);
                    return true;
                }
                Thread.sleep(POLL_INTERVAL);
                attempts++;
            }
            
            logger.warn("[WAIT] Element not enabled within {}ms", MEDIUM_TIMEOUT);
            return false;
            
        } catch (Exception e) {
            logger.warn("[WAIT] Error waiting for element to be enabled: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Waits for page to be fully loaded.
     * 
     * @param page The Playwright page
     */
    public static void waitForPageLoad(Page page) {
        try {
            long startTime = System.currentTimeMillis();
            
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("[WAIT] Page loaded after {}ms", duration);
            
        } catch (Exception e) {
            logger.warn("[WAIT] Page load timeout: {}", e.getMessage());
        }
    }
    
    /**
     * Waits for network to be idle (no ongoing requests).
     * Useful after AJAX calls or dynamic content loading.
     * 
     * @param page The Playwright page
     */
    public static void waitForNetworkIdle(Page page) {
        try {
            long startTime = System.currentTimeMillis();
            
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("[WAIT] Network idle after {}ms", duration);
            
        } catch (Exception e) {
            logger.warn("[WAIT] Network idle timeout: {}", e.getMessage());
        }
    }
    
    /**
     * Smart wait that adapts based on element state.
     * Tries short wait first, extends if element is still loading.
     * 
     * @param locator The Playwright locator
     * @param actionType The type of action
     * @return true if element is ready, false otherwise
     */
    public static boolean smartWait(Locator locator, String actionType) {
        // Try short wait first for fast interactions
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(SHORT_TIMEOUT));
            
            logger.debug("[WAIT] Fast path - Element ready quickly");
            return true;
            
        } catch (Exception e) {
            // If short wait fails, try longer wait
            logger.debug("[WAIT] Fast path failed, trying extended wait...");
            return waitForVisible(locator, actionType);
        }
    }
    
    /**
     * Waits for element to disappear (useful for loading spinners).
     * 
     * @param locator The Playwright locator
     * @return true if element disappeared, false otherwise
     */
    public static boolean waitForHidden(Locator locator) {
        try {
            long startTime = System.currentTimeMillis();
            
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(LONG_TIMEOUT));
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("[WAIT] Element hidden after {}ms", duration);
            
            return true;
        } catch (Exception e) {
            logger.warn("[WAIT] Element still visible after {}ms", LONG_TIMEOUT);
            return false;
        }
    }

    /**
     * Waits for an element to contain specific text.
     * 
     * @param locator The Playwright locator
     * @param expectedText The text to wait for
     * @return true if text found, false otherwise
     */
    public static boolean waitForText(Locator locator, String expectedText) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Wait for element to be visible first
            if (!smartWait(locator, "VERIFY_TEXT")) {
                return false;
            }

            int attempts = 0;
            int maxAttempts = MEDIUM_TIMEOUT / POLL_INTERVAL;
            
            while (attempts < maxAttempts) {
                String actualText = locator.textContent();
                if (actualText != null && actualText.contains(expectedText)) {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("[WAIT] Text '{}' found after {}ms", expectedText, duration);
                    return true;
                }
                Thread.sleep(POLL_INTERVAL);
                attempts++;
            }
            
            logger.warn("[WAIT] Expected text '{}' not found within {}ms", expectedText, MEDIUM_TIMEOUT);
            return false;
            
        } catch (Exception e) {
            logger.warn("[WAIT] Error waiting for text: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Determines appropriate timeout based on action type.
     * 
     * @param actionType The type of action being performed
     * @return Timeout in milliseconds
     */
    private static int getTimeoutForAction(String actionType) {
        if (actionType == null) {
            return DEFAULT_TIMEOUT;
        }
        
        switch (actionType.toUpperCase()) {
            case "NAVIGATE":
            case "WAIT_NAVIGATION":
                return LONG_TIMEOUT;
                
            case "VERIFY_TEXT":
            case "VERIFY_ELEMENT":
                return MEDIUM_TIMEOUT;
                
            case "CLICK":
            case "TYPE":
                return DEFAULT_TIMEOUT;
                
            case "SCROLL":
                return SHORT_TIMEOUT;
                
            default:
                return DEFAULT_TIMEOUT;
        }
    }
    
    /**
     * Custom wait with condition checker.
     * Polls until condition is true or timeout.
     * 
     * @param condition The condition to check
     * @param timeout Timeout in milliseconds
     * @param errorMessage Error message if timeout occurs
     * @return true if condition met, false if timeout
     */
    public static boolean waitForCondition(java.util.function.BooleanSupplier condition, 
                                          int timeout, String errorMessage) {
        long startTime = System.currentTimeMillis();
        int attempts = 0;
        int maxAttempts = timeout / POLL_INTERVAL;
        
        while (attempts < maxAttempts) {
            try {
                if (condition.getAsBoolean()) {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("[WAIT] Condition met after {}ms", duration);
                    return true;
                }
                Thread.sleep(POLL_INTERVAL);
                attempts++;
            } catch (Exception e) {
                logger.warn("[WAIT] Error checking condition: {}", e.getMessage());
            }
        }
        
        logger.warn("[WAIT] Condition not met: {}", errorMessage);
        return false;
    }
}
