package com.framework.executor;

import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.reporting.ErrorReporter;
import com.framework.strategy.LocatorStrategy;
import com.framework.healing.SmartLocatorFinder;
import com.framework.strategy.SmartWaitStrategy;
import com.framework.utils.ParameterExtractor;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles user interactions like click, type, hover, etc.
 */
public class InteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(InteractionHandler.class);

    public static boolean executeClick(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) {
            logger.error("[ERROR] No element locators for click action");
            return false;
        }
        
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) {
                ErrorReporter.reportLocatorError("Click action", 
                    locators.getBestLocator(), null, "Element not found with any locator");
                return false;
            }
            
            if (!SmartWaitStrategy.smartWait(element, "CLICK")) {
                logger.warn("[WARN] Element not ready within timeout, attempting click anyway");
            }
            
            element.click();
            SmartWaitStrategy.waitForPageLoad(page);
            
            String successfulLocator = LocatorStrategy.getSuccessfulSelector(page, locators);
            logger.debug("[OK] Clicked: {}", successfulLocator);
            return true;
        } catch (Exception e) {
            ErrorReporter.reportStepError("Click action", "CLICK", "Failed to click element", e);
            return false;
        }
    }

    public static boolean executeType(Page page, ActionData action, String originalGherkinStep) {
        ElementLocators locators = action.getElement();
        if (locators == null) {
            logger.error("[ERROR] No element locators for type action");
            return false;
        }
        
        String value = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (value == null) value = action.getValue();
        
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) {
                ErrorReporter.reportLocatorError(originalGherkinStep, 
                    locators.getBestLocator(), value, "Element not found with any locator");
                return false;
            }
            
            if (!SmartWaitStrategy.waitForEnabled(element)) {
                logger.warn("[WARN] Element not enabled, attempting type anyway");
            }
            
            element.fill(value);
            String successfulLocator = LocatorStrategy.getSuccessfulSelector(page, locators);
            logger.debug("[OK] Typed '{}' into: {}", value, successfulLocator);
            return true;
        } catch (Exception e) {
            ErrorReporter.reportLocatorError(originalGherkinStep, locators.getBestLocator(), value, 
                "Failed to type into element: " + e.getMessage());
            return false;
        }
    }

    public static boolean executeDoubleClick(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            if (!SmartWaitStrategy.smartWait(element, "CLICK")) {
                logger.warn("[WARN] Element not ready for double click");
            }
            element.dblclick();
            logger.debug("[OK] Double-clicked: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Double click failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeRightClick(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.click(new Locator.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
            logger.debug("[OK] Right-clicked: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Right click failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeClear(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.clear();
            logger.debug("[OK] Cleared field: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Clear failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeSelect(Page page, ActionData action, String originalGherkinStep) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        String val = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (val == null) val = action.getValue();
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.selectOption(val);
            logger.debug("[OK] Selected '{}' from: {}", val, locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Select failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeHover(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.hover();
            logger.debug("[OK] Hovered over: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Hover failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeCheck(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.check();
            logger.debug("[OK] Checked: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Check failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeUncheck(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            element.uncheck();
            logger.debug("[OK] Unchecked: {}", locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Uncheck failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executePressKey(Page page, ActionData action, String originalGherkinStep) {
        String key = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        if (key == null) key = action.getValue();
        try {
            page.keyboard().press(key);
            logger.debug("[OK] Pressed key: {}", key);
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Press key failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeDragDrop(Page page, ActionData action) {
        ElementLocators source = action.getElement();
        ElementLocators target = action.getTargetElement();
        if (source == null || target == null) return false;
        try {
            Locator sourceEl = SmartLocatorFinder.findElement(page, source);
            Locator targetEl = SmartLocatorFinder.findElement(page, target);
            if (sourceEl == null || targetEl == null) return false;
            sourceEl.dragTo(targetEl);
            logger.debug("[OK] Dragged from {} to {}", source.getBestLocator(), target.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Drag and drop failed: {}", e.getMessage());
            return false;
        }
    }
}
