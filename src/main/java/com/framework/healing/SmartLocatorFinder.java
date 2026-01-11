package com.framework.healing;

import com.framework.data.ElementLocators;
import com.framework.strategy.LocatorStrategy;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smart Finder that orchestrates both traditional locators and self-healing.
 */
public class SmartLocatorFinder {
    private static final Logger logger = LoggerFactory.getLogger(SmartLocatorFinder.class);

    /**
     * Finds an element by first trying all standard locators, 
     * and falling back to the Self-Healing Engine if they fail.
     */
    public static Locator findElement(Page page, ElementLocators locators) {
        // Step 1: Try Standard Fallback (Fast Path)
        Locator element = LocatorStrategy.findElementWithFallback(page, locators);
        
        // Step 2: If failed, try Self-Healing (Slow/Safe Path)
        if (element == null) {
            logger.warn("[FINDER] Standard locators failed. Invoking Self-Healing Engine...");
            
            // Capture full "Before" state for all 17 attributes
            ElementLocators before = copyLocators(locators);

            SelfHealingEngine.HealedResult result = SelfHealingEngine.attemptHealing(page, locators);
            
            if (result != null) {
                element = result.locator;
                
                // Update locators object so it can be saved back to JSON
                locators.setSelector(result.selector);
                locators.setHealed(true);
                
                // Enrich immediately to get "After" state
                com.framework.data.StepRepository.populateLiveAttributes(element, locators);

                // Generate Comprehensive Healing Report
                StringBuilder report = new StringBuilder();
                report.append("\n====================================================\n");
                report.append("           COMPREHENSIVE HEALING REPORT             \n");
                report.append("====================================================\n");
                report.append(String.format(" %-15s | %-18s | %-18s\n", "ATTRIBUTE", "BEFORE (BROKEN)", "AFTER (HEALED)"));
                report.append("----------------------------------------------------\n");

                addReportLine(report, "ID", before.getId(), locators.getId());
                addReportLine(report, "Name", before.getName(), locators.getName());
                addReportLine(report, "XPath", before.getXpath(), locators.getXpath());
                addReportLine(report, "Selector", before.getSelector(), locators.getSelector());
                addReportLine(report, "Role", before.getRole(), locators.getRole());
                addReportLine(report, "ARIA Label", before.getAriaLabel(), locators.getAriaLabel());
                addReportLine(report, "Placeholder", before.getPlaceholder(), locators.getPlaceholder());
                addReportLine(report, "Text", before.getText(), locators.getText());
                addReportLine(report, "Title", before.getTitle(), locators.getTitle());
                addReportLine(report, "Value", before.getValue(), locators.getValue());
                addReportLine(report, "href", before.getHref(), locators.getHref());
                addReportLine(report, "src", before.getSrc(), locators.getSrc());
                addReportLine(report, "Class", before.getClassName(), locators.getClassName());

                report.append("====================================================");
                logger.info(report.toString());
            }
        } else {
            // Step 3: Regular execution - Always enrich for "Strong" repo
            com.framework.data.StepRepository.populateLiveAttributes(element, locators);
        }
        
        return element;
    }

    private static void addReportLine(StringBuilder sb, String label, String before, String after) {
        String b = (before == null || before.isEmpty()) ? "null" : before;
        String a = (after == null || after.isEmpty()) ? "null" : after;
        
        // ONLY log if the attribute actually changed (Healed)
        if (!b.equals(a)) {
            sb.append(String.format(" %-15s | %-18s | %-18s\n", label, truncate(b, 18), truncate(a, 18)));
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) return "null";
        return text.length() <= max ? text : text.substring(0, max-3) + "...";
    }

    private static ElementLocators copyLocators(ElementLocators original) {
        ElementLocators copy = new ElementLocators();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setXpath(original.getXpath());
        copy.setSelector(original.getSelector());
        copy.setRole(original.getRole());
        copy.setAriaLabel(original.getAriaLabel());
        copy.setPlaceholder(original.getPlaceholder());
        copy.setText(original.getText());
        copy.setTitle(original.getTitle());
        copy.setValue(original.getValue());
        copy.setHref(original.getHref());
        copy.setSrc(original.getSrc());
        copy.setClassName(original.getClassName());
        return copy;
    }

    /**
     * Gets the selector used. If healed, returns the healed description.
     */
    public static String getEffectiveSelector(Page page, ElementLocators locators) {
        String selector = LocatorStrategy.getSuccessfulSelector(page, locators);
        if (selector == null && locators.getFingerprint() != null) {
            return "Healed-Element: " + locators.getFingerprint().getAttributes().getAriaLabel();
        }
        return selector;
    }
}
