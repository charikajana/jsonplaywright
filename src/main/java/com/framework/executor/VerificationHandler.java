package com.framework.executor;

import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.reporting.ErrorReporter;
import com.framework.utils.ParameterExtractor;
import com.framework.healing.SmartLocatorFinder;
import com.framework.strategy.SmartWaitStrategy;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles verification actions like verify text, verify element visibility, etc.
 */
public class VerificationHandler {
    private static final Logger logger = LoggerFactory.getLogger(VerificationHandler.class);

    public static boolean executeVerifyText(Page page, ActionData action, String originalGherkinStep) {
        ElementLocators locators = action.getElement();
        if (locators == null) {
            logger.error("[ERROR] No element locators for verify action");
            return false;
        }
        
        String locator = locators.getBestLocator();
        String expectedText = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (expectedText == null) expectedText = action.getExpectedText();
        
        // Handle runtime parameter placeholders if no parameter was extracted from Gherkin
        if ("___RUNTIME_PARAMETER___".equals(expectedText) && locators.getText() != null) {
            logger.debug("[VERIFY] No runtime parameter provided, falling back to recorded text: {}", locators.getText());
            expectedText = locators.getText();
        }
        
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) {
                logger.error("[ERROR] Element not found for verification: {}", locator);
                return false;
            }
            
            // Wait specifically for the text to appear
            if (SmartWaitStrategy.waitForText(element, expectedText)) {
                logger.debug("[OK] Text verified: \"{}\" found", expectedText);
                return true;
            } else {
                String actualText = element.textContent();
                logger.error("[ERROR] Expected: \"{}\", Got: \"{}\"", expectedText, actualText);
                ErrorReporter.reportVerificationError(originalGherkinStep, expectedText, actualText, locator);
                return false;
            }
        } catch (Exception e) {
            ErrorReporter.reportStepError(originalGherkinStep, "VERIFY_TEXT", "Failed to verify text", e);
            return false;
        }
    }

    public static boolean executeVerifyElement(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        
        Locator element = SmartLocatorFinder.findElement(page, locators);
        if (element != null && element.isVisible()) {
            logger.debug("[OK] Element verified visible: {}", locators.getBestLocator());
            return true;
        } else {
            logger.error("[ERROR] Element not visible or not found: {}", locators.getBestLocator());
            return false;
        }
    }

    public static boolean executeVerifyElements(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        String locator = locators.getBestLocator();
        Integer expectedCount = action.getExpectedCount();
        try {
            int actualCount = page.locator(locator).count();
            if (expectedCount != null) {
                if (actualCount == expectedCount) {
                    logger.debug("[OK] Found {} elements as expected: {}", actualCount, locator);
                    return true;
                } else {
                    logger.error("[ERROR] Expected {} elements, found {}: {}", expectedCount, actualCount, locator);
                    return false;
                }
            } else {
                if (actualCount > 0) {
                    logger.debug("[OK] Found {} elements: {}", actualCount, locator);
                    return true;
                } else {
                    logger.error("[ERROR] No elements found: {}", locator);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR] Error verifying elements: {}", e.getMessage());
            return false;
        }
    }
}
