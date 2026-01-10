package com.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Environment Configuration Manager
 * Handles environment-specific settings like base URLs, API endpoints, etc.
 */
public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    
    private static final String DEFAULT_ENV = "DEV";
    private static String currentEnvironment;
    private static final Map<String, Properties> envProperties = new HashMap<>();
    
    static {
        // Initialize environment from system property or default to DEV
        currentEnvironment = System.getProperty("test.environment", DEFAULT_ENV).toUpperCase();
        logger.info("Environment set to: {}", currentEnvironment);
        loadEnvironmentProperties();
    }
    
    /**
     * Load environment-specific properties
     */
    private static void loadEnvironmentProperties() {
        String configFile = "src/test/resources/env/" + currentEnvironment.toUpperCase() + ".properties";
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            envProperties.put(currentEnvironment, props);
            logger.info("Loaded environment config: {}", configFile);
        } catch (IOException e) {
            logger.warn("Could not load config file: {} - Using defaults", configFile);
            // Load default properties
            props.setProperty("base.url", "https://www.saucedemo.com");
            props.setProperty("api.url", "https://api.saucedemo.com");
            envProperties.put(currentEnvironment, props);
        }
    }
    
    /**
     * Get current environment name
     */
    public static String getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    /**
     * Set environment (useful for testing)
     */
    public static void setEnvironment(String env) {
        currentEnvironment = env.toUpperCase();
        if (!envProperties.containsKey(currentEnvironment)) {
            loadEnvironmentProperties();
        }
        logger.info("Switched environment to: {}", currentEnvironment);
    }
    
    /**
     * Get property value for current environment
     */
    public static String getProperty(String key) {
        Properties props = envProperties.get(currentEnvironment);
        if (props == null) {
            logger.warn("No properties loaded for environment: {}", currentEnvironment);
            return null;
        }
        return props.getProperty(key);
    }
    
    /**
     * Get property with default value
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get base URL for current environment
     */
    public static String getBaseUrl() {
        return getProperty("base.url", "https://www.saucedemo.com");
    }
    
    /**
     * Get API URL for current environment
     */
    public static String getApiUrl() {
        return getProperty("api.url", "https://api.saucedemo.com");
    }
    
    /**
     * Resolve URL placeholders with environment-specific values
     * 
     * @param urlWithPlaceholders URL containing placeholders like ${BASE_URL}/inventory.html
     * @return Resolved URL with actual environment values
     */
    public static String resolveUrl(String urlWithPlaceholders) {
        if (urlWithPlaceholders == null || urlWithPlaceholders.isEmpty()) {
            return urlWithPlaceholders;
        }
        
        String resolved = urlWithPlaceholders;
        
        // Replace ${BASE_URL} placeholder
        if (resolved.contains("${BASE_URL}")) {
            resolved = resolved.replace("${BASE_URL}", getBaseUrl());
        }
        
        // Replace ${API_URL} placeholder
        if (resolved.contains("${API_URL}")) {
            resolved = resolved.replace("${API_URL}", getApiUrl());
        }
        
        // Replace any other custom placeholders
        Properties props = envProperties.get(currentEnvironment);
        if (props != null) {
            for (String key : props.stringPropertyNames()) {
                String placeholder = "${" + key.toUpperCase().replace(".", "_") + "}";
                if (resolved.contains(placeholder)) {
                    resolved = resolved.replace(placeholder, props.getProperty(key));
                }
            }
        }
        
        return resolved;
    }
    
    /**
     * Convert absolute URL to parameterized URL
     * Replaces known base URLs with placeholders
     * 
     * @param absoluteUrl Absolute URL like https://www.saucedemo.com/inventory.html
     * @return Parameterized URL like ${BASE_URL}/inventory.html
     */
    public static String parameterizeUrl(String absoluteUrl) {
        if (absoluteUrl == null || absoluteUrl.isEmpty()) {
            return absoluteUrl;
        }
        
        String parameterized = absoluteUrl;
        
        // Get all known base URLs from all environments
        for (Properties props : envProperties.values()) {
            String baseUrl = props.getProperty("base.url");
            if (baseUrl != null && parameterized.startsWith(baseUrl)) {
                parameterized = parameterized.replace(baseUrl, "${BASE_URL}");
                break;
            }
            
            String apiUrl = props.getProperty("api.url");
            if (apiUrl != null && parameterized.startsWith(apiUrl)) {
                parameterized = parameterized.replace(apiUrl, "${API_URL}");
                break;
            }
        }
        
        return parameterized;
    }
    
    /**
     * Check if URL contains placeholders
     */
    public static boolean hasPlaceholders(String url) {
        return url != null && url.contains("${");
    }
    
    /**
     * Get all environment properties
     */
    public static Map<String, String> getAllProperties() {
        Properties props = envProperties.get(currentEnvironment);
        Map<String, String> result = new HashMap<>();
        if (props != null) {
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
        }
        return result;
    }
}
