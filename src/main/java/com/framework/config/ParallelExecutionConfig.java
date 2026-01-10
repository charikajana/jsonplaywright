package com.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for parallel test execution.
 * Defines thread pool settings and browser isolation strategies.
 */
public class ParallelExecutionConfig {
    private static final Logger logger = LoggerFactory.getLogger(ParallelExecutionConfig.class);
    
    // Default thread count for parallel execution
    private static final int DEFAULT_THREAD_COUNT = 4;
    
    // Maximum thread count to prevent resource exhaustion
    private static final int MAX_THREAD_COUNT = 10;
    
    // Minimum thread count
    private static final int MIN_THREAD_COUNT = 1;
    
    /**
     * Gets the thread count for parallel execution from system properties.
     * Falls back to DEFAULT_THREAD_COUNT if not specified or invalid.
     * 
     * @return Number of threads to use for parallel execution
     */
    public static int getThreadCount() {
        String threadCountProp = System.getProperty("parallel.threads");
        
        if (threadCountProp != null && !threadCountProp.isEmpty()) {
            try {
                int count = Integer.parseInt(threadCountProp);
                
                if (count < MIN_THREAD_COUNT) {
                    logger.warn("[WARN] Thread count {} is below minimum. Using {}", count, MIN_THREAD_COUNT);
                    return MIN_THREAD_COUNT;
                }
                
                if (count > MAX_THREAD_COUNT) {
                    logger.warn("[WARN] Thread count {} exceeds maximum. Using {}", count, MAX_THREAD_COUNT);
                    return MAX_THREAD_COUNT;
                }
                
                logger.info("[INFO] Using {} threads for parallel execution", count);
                return count;
            } catch (NumberFormatException e) {
                logger.warn("[WARN] Invalid thread count: {}. Using default: {}", threadCountProp, DEFAULT_THREAD_COUNT);
            }
        }
        
        return DEFAULT_THREAD_COUNT;
    }
    
    /**
     * Checks if parallel execution is enabled via system property.
     * 
     * @return true if parallel execution is enabled, false otherwise
     */
    public static boolean isParallelEnabled() {
        String parallel = System.getProperty("parallel.enabled", "false");
        return "true".equalsIgnoreCase(parallel);
    }
    
    /**
     * Gets the parallel execution mode (methods, classes, or suites).
     * 
     * @return The parallel execution mode
     */
    public static String getParallelMode() {
        return System.getProperty("parallel.mode", "methods");
    }
    
    /**
     * Prints the current parallel execution configuration.
     */
    public static void printConfiguration() {
        logger.info("╔═══════════════════════════════════════════════════╗");
        logger.info("║       PARALLEL EXECUTION CONFIGURATION            ║");
        logger.info("╚═══════════════════════════════════════════════════╝");
        logger.info("  Parallel Enabled: {}", isParallelEnabled());
        logger.info("  Thread Count:     {}", getThreadCount());
        logger.info("  Parallel Mode:    {}", getParallelMode());
        logger.info("═════════════════════════════════════════════════════");
    }
}
