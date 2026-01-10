package com.framework.strategy;

import com.framework.data.ElementLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart locator strategy with fallback mechanism.
 * Attempts multiple locators in priority order until one succeeds.
 * 
 * Priority Order:
 * 1. id (unique identifier)
 * 2. name (stable for form elements)
 * 3. CSS selector
 * 4. Generic selector
 * 5. XPath
 * 6. Text content
 * 7. Placeholder
 * 8. data-testid (last priority)
 */
public class LocatorStrategy {
    private static final Logger logger = LoggerFactory.getLogger(LocatorStrategy.class);
    
    /**
     * Finds an element using fallback strategy.
     * Tries each locator in priority order until one is found.
     * 
     * @param page Playwright page object
     * @param locators Element locators from JSON
     * @return Locator object if found, null otherwise
     */
    public static Locator findElementWithFallback(Page page, ElementLocators locators) {
        List<LocatorAttempt> attempts = buildLocatorAttempts(locators);
        
        logger.debug("[LOCATOR] Attempting {} fallback strategies", attempts.size());
        
        for (LocatorAttempt attempt : attempts) {
            try {
                Locator locator = page.locator(attempt.selector);
                
                // Check if element exists
                if (locator.count() > 0) {
                    logger.debug("[LOCATOR] ✓ Found element using: {} -> {}", 
                        attempt.strategy, attempt.selector);
                    return locator;
                }
                
                logger.debug("[LOCATOR] ✗ Not found using {}: {}", 
                    attempt.strategy, attempt.selector);
                
            } catch (Exception e) {
                logger.debug("[LOCATOR] ✗ Error with {}: {}", 
                    attempt.strategy, e.getMessage());
            }
        }
        
        logger.error("[LOCATOR] ❌ FAILED - Element not found after {} attempts", attempts.size());
        return null;
    }
    
    /**
     * Gets the actual selector string that was successfully used.
     * 
     * @param page Playwright page
     * @param locators Element locators
     * @return The successful selector string
     */
    public static String getSuccessfulSelector(Page page, ElementLocators locators) {
        List<LocatorAttempt> attempts = buildLocatorAttempts(locators);
        
        for (LocatorAttempt attempt : attempts) {
            try {
                if (page.locator(attempt.selector).count() > 0) {
                    return attempt.selector;
                }
            } catch (Exception e) {
                // Continue to next
            }
        }
        
        return null;
    }
    
    /**
     * Builds a prioritized list of locator attempts.
     */
    private static List<LocatorAttempt> buildLocatorAttempts(ElementLocators locators) {
        List<LocatorAttempt> attempts = new ArrayList<>();
        
        // Priority 1: ID (usually unique and stable)
        if (locators.getId() != null && !locators.getId().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "id", 
                "#" + locators.getId()
            ));
        }
        
        // Priority 2: Name (stable for form elements)
        if (locators.getName() != null && !locators.getName().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "name", 
                "[name='" + locators.getName() + "']"
            ));
        }
        
        // Priority 3: CSS Selector
        if (locators.getCssSelector() != null && !locators.getCssSelector().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "css", 
                locators.getCssSelector()
            ));
        }
        
        // Priority 4: Generic selector from JSON
        if (locators.getSelector() != null && !locators.getSelector().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "selector", 
                locators.getSelector()
            ));
        }
        
        // Priority 5: XPath
        if (locators.getXpath() != null && !locators.getXpath().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "xpath", 
                locators.getXpath()
            ));
        }
        
        // Priority 6: Text content
        if (locators.getText() != null && !locators.getText().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "text", 
                "text=" + locators.getText()
            ));
        }
        
        // Priority 7: Placeholder (for input fields)
        if (locators.getPlaceholder() != null && !locators.getPlaceholder().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "placeholder", 
                "[placeholder='" + locators.getPlaceholder() + "']"
            ));
        }
        
        // Priority 8: data-testid (last in priority)
        if (locators.getDataTest() != null && !locators.getDataTest().isEmpty()) {
            attempts.add(new LocatorAttempt(
                "data-testid", 
                "[data-testid='" + locators.getDataTest() + "']"
            ));
        }
        
        return attempts;
    }
    
    /**
     * Internal class to represent a locator attempt.
     */
    private static class LocatorAttempt {
        final String strategy;
        final String selector;
        
        LocatorAttempt(String strategy, String selector) {
            this.strategy = strategy;
            this.selector = selector;
        }
    }
}
