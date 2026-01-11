package com.framework.reporting;

import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced error reporting utility for test failures.
 * Provides detailed error information in both logs and Allure reports.
 */
public class ErrorReporter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorReporter.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Reports a step execution error with detailed context.
     * 
     * @param stepText The Gherkin step text
     * @param actionType The type of action being performed
     * @param errorMessage The error message
     * @param exception Optional exception that was thrown
     */
    public static void reportStepError(String stepText, String actionType, 
                                      String errorMessage, Throwable exception) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        // Build detailed error report
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║          STEP EXECUTION FAILURE                        ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");
        report.append("📅 Timestamp: ").append(timestamp).append("\n");
        report.append("📍 Gherkin Step: ").append(stepText).append("\n");
        report.append("🔧 Action Type: ").append(actionType).append("\n");
        report.append("❌ Error: ").append(errorMessage).append("\n");
        
        if (exception != null) {
            report.append("🔍 Exception Type: ").append(exception.getClass().getSimpleName()).append("\n");
            report.append("📝 Exception Message: ").append(exception.getMessage()).append("\n");
            
            if (exception.getCause() != null) {
                report.append("🔗 Root Cause: ").append(exception.getCause().getMessage()).append("\n");
            }
        }
        
        // Log to console
        logger.error("\n{}", report);
        
        // Attach to Allure report
        Allure.addAttachment("Step Failure Details", "text/plain", report.toString(), ".txt");
    }
    
    /**
     * Reports a locator-related error with element details.
     * 
     * @param stepText The Gherkin step text
     * @param locator The locator that failed
     * @param value Optional value being used
     * @param errorMessage The error message
     */
    public static void reportLocatorError(String stepText, String locator, 
                                         String value, String errorMessage) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║          ELEMENT LOCATOR FAILURE                       ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");
        report.append("📅 Timestamp: ").append(timestamp).append("\n");
        report.append("📍 Gherkin Step: ").append(stepText).append("\n");
        report.append("🎯 Locator: ").append(locator).append("\n");
        
        if (value != null && !value.isEmpty()) {
            report.append("💾 Value Used: ").append(value).append("\n");
        }
        
        report.append("Error: ").append(errorMessage).append("\n\n");
        report.append("Troubleshooting Tips:\n");
        report.append("   - Verify the element exists on the page\n");
        report.append("   - Check if the locator selector is correct\n");
        report.append("   - Ensure the element is visible and enabled\n");
        report.append("   - Consider adding an explicit wait\n");
        
        logger.error("\n{}", report);
        Allure.addAttachment("Locator Failure Details", "text/plain", report.toString(), ".txt");
    }
    
    /**
     * Reports a verification/assertion error with expected vs actual values.
     * 
     * @param stepText The Gherkin step text
     * @param expected The expected value
     * @param actual The actual value
     * @param locator Optional locator where the check was performed
     */
    public static void reportVerificationError(String stepText, String expected, 
                                              String actual, String locator) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║          VERIFICATION FAILURE                          ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");
        report.append("📅 Timestamp: ").append(timestamp).append("\n");
        report.append("📍 Gherkin Step: ").append(stepText).append("\n");
        
        if (locator != null && !locator.isEmpty()) {
            report.append("🎯 Locator: ").append(locator).append("\n");
        }
        
        report.append("\n");
        report.append("Expected: ").append(expected).append("\n");
        report.append("Actual:   ").append(actual).append("\n\n");
        
        // Calculate similarity for helpful debugging
        if (expected != null && actual != null) {
            boolean containsExpected = actual.contains(expected);
            boolean caseInsensitiveMatch = actual.equalsIgnoreCase(expected);
            
            report.append("Analysis:\n");
            if (containsExpected) {
                report.append("   [PASSED] Actual text CONTAINS expected text\n");
            } else {
                report.append("   [FAILED] Actual text DOES NOT contain expected text\n");
            }
            
            if (caseInsensitiveMatch) {
                report.append("   [WARNING] Case-insensitive match found (check case sensitivity)\n");
            }
        }
        
        logger.error("\n{}", report);
        Allure.addAttachment("Verification Failure", "text/plain", report.toString(), ".txt");
    }
    
    /**
     * Reports a parameterization error.
     * 
     * @param stepText The Gherkin step text
     * @param extractedParams The parameters that were extracted
     * @param errorMessage The error message
     */
    public static void reportParameterError(String stepText, 
                                           java.util.List<String> extractedParams, 
                                           String errorMessage) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║          PARAMETER EXTRACTION ERROR                    ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");
        report.append("📅 Timestamp: ").append(timestamp).append("\n");
        report.append("📍 Gherkin Step: ").append(stepText).append("\n");
        report.append("Error: ").append(errorMessage).append("\n\n");
        report.append("Extracted Parameters: ").append(extractedParams.size()).append("\n");
        
        for (int i = 0; i < extractedParams.size(); i++) {
            report.append("   [").append(i).append("]: \"").append(extractedParams.get(i)).append("\"\n");
        }
        
        logger.error("\n{}", report);
        Allure.addAttachment("Parameter Error", "text/plain", report.toString(), ".txt");
    }
    
    /**
     * Creates a consolidated error summary for the scenario.
     * 
     * @param scenario The Cucumber scenario
     * @param errorDetails Map of error details
     */
    public static void reportScenarioError(Scenario scenario, Map<String, String> errorDetails) {
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║          SCENARIO FAILURE SUMMARY                      ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");
        report.append("Scenario: ").append(scenario.getName()).append("\n");
        report.append("Feature: ").append(scenario.getUri()).append("\n");
        report.append("Tags: ").append(scenario.getSourceTagNames()).append("\n");
        report.append("Status: FAILED\n\n");
        
        report.append("Error Details:\n");
        errorDetails.forEach((key, value) -> 
            report.append("   - ").append(key).append(": ").append(value).append("\n"));
        
        logger.error("\n{}", report);
        Allure.addAttachment("Scenario Summary", "text/plain", report.toString(), ".txt");
    }
}
