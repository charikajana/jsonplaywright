package com.framework.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry configuration and utilities for handling flaky tests.
 * Can be integrated with TestNG IRetryAnalyzer when needed.
 */
public class RetryConfig {
    private static final Logger logger = LoggerFactory.getLogger(RetryConfig.class);
    
    private static final int MAX_RETRY_COUNT = getMaxRetryCount();
    
    /**
     * Gets the maximum retry count from system properties or uses default.
     * 
     * @return Maximum number of retries
     */
    public static int getMaxRetryCount() {
        String retryProp = System.getProperty("retry.maxAttempts");
        if (retryProp != null) {
            try {
                int count = Integer.parseInt(retryProp);
                if (count >= 0 && count <= 1) {
                    logger.info("[RETRY] Using max retry count: {}", count);
                    return count;
                }
            } catch (NumberFormatException e) {
                logger.warn("[RETRY] Invalid retry count: {}. Using default: 2", retryProp);
            }
        }
        return 1; // Default: retry up to 2 times
    }
    
    /**
     * Checks if retry is enabled via system properties.
     * 
     * @return true if retry is enabled
     */
    public static boolean isRetryEnabled() {
        String retryEnabled = System.getProperty("retry.enabled", "false");
        return "true".equalsIgnoreCase(retryEnabled);
    }
    
    /**
     * Logs retry attempt information.
     * 
     * @param testName Name of the test
     * @param attemptNumber Current attempt number
     * @param reason Failure reason
     */
    public static void logRetryAttempt(String testName, int attemptNumber, String reason) {
        logger.warn("╔════════════════════════════════════════════════════════╗");
        logger.warn("║          RETRYING FAILED TEST                          ║");
        logger.warn("╚════════════════════════════════════════════════════════╝");
        logger.warn("  Test: {}", testName);
        logger.warn("  Retry Attempt: {}/{}", attemptNumber, MAX_RETRY_COUNT);
        logger.warn("  Failure Reason: {}", reason);
        logger.warn("════════════════════════════════════════════════════════");
    }
    
    /**
     * Logs max retries exceeded.
     * 
     * @param testName Name of the test
     * @param totalAttempts Total number of attempts made
     */
    public static void logMaxRetriesExceeded(String testName, int totalAttempts) {
        logger.error("╔════════════════════════════════════════════════════════╗");
        logger.error("║          MAX RETRIES EXCEEDED                          ║");
        logger.error("╚════════════════════════════════════════════════════════╝");
        logger.error("  Test: {}", testName);
        logger.error("  Total Attempts: {}", totalAttempts);
        logger.error("  Final Status: FAILED");
        logger.error("════════════════════════════════════════════════════════");
    }
}
