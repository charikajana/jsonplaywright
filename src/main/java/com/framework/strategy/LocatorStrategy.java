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
        
        Locator firstMultiMatch = null;
        String firstMultiMatchStrategy = null;
        String firstMultiMatchSelector = null;

        for (LocatorAttempt attempt : attempts) {
            try {
                Locator locator = page.locator(attempt.selector);
                int count = locator.count();
                
                if (count == 1) {
                    logger.debug("[LOCATOR] OK: Found unique element using: {} -> {}", 
                        attempt.strategy, attempt.selector);
                    return locator;
                } else if (count > 1) {
                    logger.debug("[LOCATOR] MULTIPLE ({}): Found multiple elements using: {} -> {}. Searching for unique match...", 
                        count, attempt.strategy, attempt.selector);
                    if (firstMultiMatch == null) {
                        firstMultiMatch = locator;
                        firstMultiMatchStrategy = attempt.strategy;
                        firstMultiMatchSelector = attempt.selector;
                    }
                } else {
                    logger.debug("[LOCATOR] FAIL: Not found using {}: {}", 
                        attempt.strategy, attempt.selector);
                }
            } catch (Exception e) {
                logger.debug("[LOCATOR] FAIL: Error with {}: {}", 
                    attempt.strategy, e.getMessage());
            }
        }
        
        if (firstMultiMatch != null) {
            logger.warn("[LOCATOR] WARNING: No unique element found. Using first successful (but non-unique) strategy: {} -> {}", 
                firstMultiMatchStrategy, firstMultiMatchSelector);
            return firstMultiMatch;
        }

        logger.error("[LOCATOR] FAILED - Element not found after {} attempts", attempts.size());
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
        
        // Priority 0: PROXIMITY ANCHOR (The "E-commerce Card" Hero)
        // If we have nearbyText (e.g. Product Name) and a selector/id, 
        // try finding the element near that text first.
        if (locators.getFingerprint() != null && locators.getFingerprint().getContext() != null) {
            String anchorText = locators.getFingerprint().getContext().getNearbyText();
            if (anchorText != null && !anchorText.isEmpty()) {
                String baseSelector = null;
                if (locators.getId() != null) baseSelector = "#" + locators.getId();
                else if (locators.getSelector() != null) baseSelector = locators.getSelector();
                
                if (baseSelector != null && !baseSelector.contains(":near")) {
                    // Playwright's near() selector: Finds baseSelector closest to anchorText
                    attempts.add(new LocatorAttempt("proximity", baseSelector + ":near(:text(\"" + anchorText + "\"))"));
                }
            }
        }

        // Priority 1: ID
        if (locators.getId() != null && !locators.getId().isEmpty()) {
            attempts.add(new LocatorAttempt("id", "#" + locators.getId()));
        }
        
        // Priority 2: Data Test (Automation best practice)
        if (locators.getDataTest() != null && !locators.getDataTest().isEmpty()) {
            attempts.add(new LocatorAttempt("data-test", "[data-test='" + locators.getDataTest() + "']"));
        }

        // Priority 3: Name (stable for form elements)
        if (locators.getName() != null && !locators.getName().isEmpty()) {
            attempts.add(new LocatorAttempt("name", "[name='" + locators.getName() + "']"));
        }

        // Priority 4: ARIA Label (Accessibility)
        if (locators.getAriaLabel() != null && !locators.getAriaLabel().isEmpty()) {
            attempts.add(new LocatorAttempt("aria-label", "aria-label=\"" + locators.getAriaLabel() + "\""));
        }

        // Priority 5: Role (Semantic anchor)
        if (locators.getRole() != null && !locators.getRole().isEmpty()) {
            attempts.add(new LocatorAttempt("role", "[role='" + locators.getRole() + "']"));
        }
        
        // Priority 6: CSS Selector
        if (locators.getCssSelector() != null && !locators.getCssSelector().isEmpty()) {
            attempts.add(new LocatorAttempt("css", locators.getCssSelector()));
        }
        
        // Priority 7: Generic selector 
        if (locators.getSelector() != null && !locators.getSelector().isEmpty()) {
            attempts.add(new LocatorAttempt("selector", locators.getSelector()));
        }
        
        // Priority 8: XPath
        if (locators.getXpath() != null && !locators.getXpath().isEmpty()) {
            attempts.add(new LocatorAttempt("xpath", locators.getXpath()));
        }

        // Priority 9: href (for links)
        if (locators.getHref() != null && !locators.getHref().isEmpty()) {
            attempts.add(new LocatorAttempt("href", "[href='" + locators.getHref() + "']"));
        }

        // Priority 10: src (for images/media)
        if (locators.getSrc() != null && !locators.getSrc().isEmpty()) {
            attempts.add(new LocatorAttempt("src", "[src='" + locators.getSrc() + "']"));
        }
        
        // Priority 11: Text content
        if (locators.getText() != null && !locators.getText().isEmpty()) {
            attempts.add(new LocatorAttempt("text", "text=" + locators.getText()));
        }

        // Priority 12: Value (for inputs/buttons)
        if (locators.getValue() != null && !locators.getValue().isEmpty()) {
            attempts.add(new LocatorAttempt("value", "[value='" + locators.getValue() + "']"));
        }
        
        // Priority 13: Title
        if (locators.getTitle() != null && !locators.getTitle().isEmpty()) {
            attempts.add(new LocatorAttempt("title", "[title='" + locators.getTitle() + "']"));
        }

        // Priority 14: Alt (for images)
        if (locators.getAlt() != null && !locators.getAlt().isEmpty()) {
            attempts.add(new LocatorAttempt("alt", "[alt='" + locators.getAlt() + "']"));
        }

        // Priority 15: Placeholder (for inputs)
        if (locators.getPlaceholder() != null && !locators.getPlaceholder().isEmpty()) {
            attempts.add(new LocatorAttempt("placeholder", "[placeholder='" + locators.getPlaceholder() + "']"));
        }

        // Priority 16: Class Name (Least stable, try last)
        if (locators.getClassName() != null && !locators.getClassName().isEmpty()) {
            String classSelector = "." + locators.getClassName().trim().replaceAll("\\s+", ".");
            attempts.add(new LocatorAttempt("class", classSelector));
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
