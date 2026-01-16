package com.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import io.qameta.allure.Allure;

/**
 * Resolves random data keywords into actual values and caches them 
 * for the duration of the test execution.
 */
public class RandomDataResolver {
    private static final Logger logger = LoggerFactory.getLogger(RandomDataResolver.class);
    private static final Random random = new Random();
    
    // In-memory cache to store generated values so they can be reused/verified later
    private static final ThreadLocal<Map<String, String>> scenarioCache = ThreadLocal.withInitial(HashMap::new);

    /**
     * Resolves a string input. If it matches a RANDOM_ or VAR_ keyword, it returns data.
     */
    public static String resolve(String input) {
        if (input == null) return null;
        
        String upperInput = input.toUpperCase();
        boolean isRandom = upperInput.startsWith("RANDOM_");
        boolean isVar = upperInput.startsWith("VAR_");
        
        if (!isRandom && !isVar) {
            return input; // Not a dynamic request, return as is
        }

        Map<String, String> cache = scenarioCache.get();

        // Check cache (standard for VAR_, and generated for RANDOM_)
        if (cache.containsKey(upperInput)) {
            String cachedValue = cache.get(upperInput);
            logger.debug("[DYNAMIC] Using cached value '{}' for keyword '{}'", cachedValue, input);
            return cachedValue;
        }
        
        if (isVar) {
            logger.warn("[DYNAMIC] Variable '{}' requested but not yet captured!", input);
            return input;
        }

        String generatedValue = "";        
        switch (upperInput) {
            case "RANDOM_FIRST_NAME":
                generatedValue = "Guest" + (random.nextInt(9000) + 1000);
                break;
            case "RANDOM_LAST_NAME":
                generatedValue = "User" + (random.nextInt(900) + 100);
                break;
            case "RANDOM_EMAIL":
                generatedValue = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
                break;
            case "RANDOM_PHONE":
                generatedValue = "9" + (100000000 + random.nextInt(900000000));
                break;
            default:
                // Handle patterns like RANDOM_NUMERIC_5
                if (upperInput.startsWith("RANDOM_NUMERIC_")) {
                    try {
                        int length = Integer.parseInt(upperInput.replace("RANDOM_NUMERIC_", ""));
                        generatedValue = generateNumeric(length);
                    } catch (NumberFormatException e) {
                        logger.warn("[RANDOM] Invalid numeric length in keyword: {}", input);
                        return input;
                    }
                } else if (upperInput.startsWith("RANDOM_ALPHABETIC_")) {
                     try {
                        int length = Integer.parseInt(upperInput.replace("RANDOM_ALPHABETIC_", ""));
                        generatedValue = generateAlphabetic(length);
                    } catch (NumberFormatException e) {
                        logger.warn("[RANDOM] Invalid alphabetic length in keyword: {}", input);
                        return input;
                    }
                } else {
                    // GENERIC FALLBACK: Handle RANDOM_CITY, RANDOM_TOWN, etc.
                    // Converts RANDOM_CITY -> City_1234
                    String label = input.substring(7); // Remove "RANDOM_"
                    if (label.isEmpty()) return input;
                    
                    // Capitalize first letter (city -> City)
                    label = label.substring(0, 1).toUpperCase() + label.substring(1).toLowerCase();
                    generatedValue = label + "_" + (random.nextInt(9000) + 1000);
                }
        }

        cache.put(upperInput, generatedValue);
        logger.info("[RANDOM] Generated '{}' for keyword '{}'", generatedValue, input);
        
        // Add to Allure report for visibility
        addAllureAttachment("Random Data Generated: " + upperInput, generatedValue);
        
        return generatedValue;
    }

    private static String generateNumeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }
    
    private static String generateAlphabetic(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public static void storeValue(String key, String value) {
        if (key == null || key.isEmpty() || "___RUNTIME_PARAMETER___".equals(key)) return;
        String upperKey = key.toUpperCase();
        scenarioCache.get().put(upperKey, value);
        logger.info("[DYNAMIC] Captured and stored variable: {} = '{}'", upperKey, value);
        
        // Add to Allure report for visibility
        addAllureAttachment("Variable Captured: " + upperKey, value);
    }

    private static void addAllureAttachment(String name, String value) {
        try {
            Allure.addAttachment(name, "text/plain", value);
        } catch (Exception e) {
            logger.debug("Allure not initialized, skipping attachment: {}", name);
        }
    }

    public static void clearCache() {
        scenarioCache.get().clear();
        logger.debug("[RANDOM] Cache cleared for current thread");
    }
}
