package com.framework.executor;

import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.reporting.ErrorReporter;
import com.framework.utils.ParameterExtractor;
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
        
        try {
            Locator element = page.locator(locator);
            String actualText = element.textContent();
            
            if (actualText.contains(expectedText)) {
                logger.debug("[OK] Text verified: \"{}\" found", expectedText);
                return true;
            } else {
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
        String locator = locators.getBestLocator();
        Locator element = page.locator(locator);
        if (element.isVisible()) {
            logger.debug("[OK] Element verified visible: {}", locator);
            return true;
        } else {
            logger.error("[ERROR] Element not visible: {}", locator);
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
