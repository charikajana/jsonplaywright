package com.framework.executor;

import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.reporting.ErrorReporter;
import com.framework.strategy.LocatorStrategy;
import com.framework.healing.SmartLocatorFinder;
import com.framework.strategy.SmartWaitStrategy;
import com.framework.utils.DateResolver;
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
            String beforeUrl = page.url();
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
            
            // Wait for potential navigation or state change
            SmartWaitStrategy.waitForPageLoad(page);
            SmartWaitStrategy.waitForNetworkIdle(page);
            
            String afterUrl = page.url();
            if (!beforeUrl.equals(afterUrl)) {
                logger.info("[OK] Navigated from {} to {}", beforeUrl, afterUrl);
            } else {
                logger.info("[INFO] Clicked but URL remained: {}", afterUrl);
            }
            
            String successfulLocator = LocatorStrategy.getSuccessfulSelector(page, locators);
            logger.debug("[OK] Clicked: {}", successfulLocator);
            return true;
        } catch (Exception e) {
            ErrorReporter.reportStepError("Click action", "CLICK", "Failed to click element", e);
            return false;
        }
    }

    /**
     * Handles clicks that open a new popup/window using Playwright's waitForPopup.
     */
    public static boolean executeClickAndSwitch(Page page, ActionData action) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;

        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;

            logger.info("[WINDOW] Clicking and waiting for popup...");
            Page popup = page.waitForPopup(() -> {
                element.click();
            });

            if (popup != null) {
                popup.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD);
                com.framework.playwright.PlaywrightManager.getInstance().setPage(popup);
                logger.info("[OK] Popup captured and switched: {}", popup.url());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("[ERROR] Click and switch failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeType(Page page, ActionData action, String originalGherkinStep, int actionIndex) {
        ElementLocators locators = action.getElement();
        if (locators == null) {
            logger.error("[ERROR] No element locators for type action");
            return false;
        }
        
        // Extract parameter at the correct index based on action number
        String rawValue = ParameterExtractor.extractParameterAtIndex(originalGherkinStep, actionIndex);
        if (rawValue == null) rawValue = action.getValue();
        
        // Check if this is a date field - if dateFormat is specified, use DateResolver
        String dateFormat = action.getDateFormat();
        String value;
        boolean isDateField = (dateFormat != null && !dateFormat.isEmpty());
        
        if (isDateField) {
            // Resolve date using DateResolver (supports "25" as 25 days from today)
            value = DateResolver.resolveDate(rawValue, dateFormat);
            logger.debug("[DATE] Resolved '{}' to '{}' using format '{}'", rawValue, value, dateFormat);
        } else {
            // Resolve random keywords for non-date fields
            value = com.framework.utils.RandomDataResolver.resolve(rawValue);
        }
        
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
            
            // Use robust typing for date fields (they often have date pickers that interfere with fill())
            if (isDateField) {
                // Focus and clear
                element.focus();
                element.fill("");
                
                // If still not empty (masked inputs), try select all and delete
                String currentVal = element.inputValue();
                if (currentVal != null && !currentVal.isEmpty()) {
                    element.click();
                    page.keyboard().press("Control+A");
                    page.keyboard().press("Backspace");
                }
                
                // Type character by character with slight delay for date pickers
                element.type(value, new Locator.TypeOptions().setDelay(30));
            } else {
                // Standard fill for non-date fields
                element.fill(value);
            }
            
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

    public static boolean executeSelect(Page page, ActionData action, String originalGherkinStep, int actionIndex) {
        return executeSelectDropdown(page, action, originalGherkinStep, actionIndex);
    }

    public static boolean executeSelectDropdown(Page page, ActionData action, String originalGherkinStep, int actionIndex) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        String rawVal = ParameterExtractor.extractParameterAtIndex(originalGherkinStep, actionIndex);
        if (rawVal == null) rawVal = action.getValue();
        
        // Resolve random keywords
        String val = com.framework.utils.RandomDataResolver.resolve(rawVal);
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            
            if (!SmartWaitStrategy.waitForEnabled(element)) {
                logger.warn("[WAIT] Dropdown not enabled: {}", locators.getBestLocator());
            }
            
            element.selectOption(val);
            logger.debug("[OK] Selected '{}' from dropdown: {}", val, locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Dropdown selection failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeSelectDate(Page page, ActionData action, String originalGherkinStep, int actionIndex) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        
        String dateInput = ParameterExtractor.extractParameterAtIndex(originalGherkinStep, actionIndex);
        if (dateInput == null) dateInput = action.getValue();
        
        // Resolve date using our new utility
        // Use M/d/yyyy format for HotelBooker compatibility (can be extended to read from JSON if needed)
        String resolvedDate = DateResolver.resolveDate(dateInput, "M/d/yyyy");
        
        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            
            if (!SmartWaitStrategy.waitForEnabled(element)) {
                logger.warn("[WAIT] Date field not enabled: {}", locators.getBestLocator());
            }

            // More robust clearing and entry for date fields
            element.focus();
            element.fill(""); // Try standard clear
            
            // If still not empty (masked inputs), try select all and backspace
            String currentVal = element.inputValue();
            if (currentVal != null && !currentVal.isEmpty()) {
                element.click();
                page.keyboard().press("Control+A");
                page.keyboard().press("Backspace");
            }

            // Type the date with a slight delay
            element.type(resolvedDate, new Locator.TypeOptions().setDelay(30));
            
            // Press Tab or Enter to trigger blur/change events
            element.press("Tab");
            
            logger.debug("[OK] Date '{}' (resolved from '{}') input into: {}", 
                resolvedDate, dateInput, locators.getBestLocator());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Date entry failed: {}", e.getMessage());
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
        // For PRESS_KEY, use the action's value directly unless it's a runtime parameter placeholder
        String rawKey = action.getValue();
        if (rawKey == null || rawKey.isEmpty() || "___RUNTIME_PARAMETER___".equals(rawKey)) {
            // Only extract from step if action value is not specified
            rawKey = ParameterExtractor.extractFirstParameter(originalGherkinStep);
        }
        
        if (rawKey == null || rawKey.isEmpty()) {
            logger.error("[ERROR] No key specified for PRESS_KEY action");
            return false;
        }
        
        String key = com.framework.utils.RandomDataResolver.resolve(rawKey);
        try {
            page.keyboard().press(key);
            logger.debug("[OK] Pressed key: {}", key);
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Press key failed: {}", e.getMessage());
            return false;
        }
    }

    public static boolean executeGetText(Page page, ActionData action, String originalGherkinStep, int actionIndex) {
        ElementLocators locators = action.getElement();
        if (locators == null) return false;
        
        String key = ParameterExtractor.extractParameterAtIndex(originalGherkinStep, actionIndex);
        if (key == null) key = action.getValue();
        
        if (key == null || key.isEmpty() || "___RUNTIME_PARAMETER___".equals(key)) {
            logger.error("[ERROR] No variable name (value) provided for GET_TEXT action");
            return false;
        }

        try {
            Locator element = SmartLocatorFinder.findElement(page, locators);
            if (element == null) return false;
            
            String text = element.innerText();
            if (text == null || text.isEmpty()) {
                text = (String) element.evaluate("el => el.value || el.placeholder || ''");
            }
            
            com.framework.utils.RandomDataResolver.storeValue(key, text.trim());
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Failed to capture text from element: {}", e.getMessage());
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
