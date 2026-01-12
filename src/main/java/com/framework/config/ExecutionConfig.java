package com.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration manager for the framework
 * Loads settings from config.properties
 */
public class ExecutionConfig {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionConfig.class);
    private static ExecutionConfig instance;
    private Properties properties;
    
    private ExecutionConfig() {
        properties = new Properties();
        loadProperties();
    }
    
    public static ExecutionConfig getInstance() {
        if (instance == null) {
            synchronized (ExecutionConfig.class) {
                if (instance == null) {
                    instance = new ExecutionConfig();
                }
            }
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.warn("config.properties not found, using defaults");
                loadDefaults();
                return;
            }
            properties.load(input);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading config.properties: " + e.getMessage());
            loadDefaults();
        }
    }
    
    private void loadDefaults() {
        properties.setProperty("browser.type", "chromium");
        properties.setProperty("browser.headless", "false");
        properties.setProperty("browser.timeout", "90000");
        properties.setProperty("cache.directory", "src/test/resources/locatorRepository");
    }
    
    // Browser Configuration
    public String getBrowserType() {
        return getProperty("browser.type", "chromium");
    }
    
    public boolean isBrowserHeadless() {
        return Boolean.parseBoolean(getProperty("browser.headless", "false"));
    }
    
    public int getBrowserTimeout() {
        return Integer.parseInt(getProperty("browser.timeout", "90000"));
    }
    
    public int getSlowMo() {
        return Integer.parseInt(getProperty("browser.slowmo", "0"));
    }
    
    // Cache Configuration
    public String getCacheDirectory() {
        return getProperty("cache.directory", "src/test/resources/locatorRepository");
    }
    
    public boolean isCacheValidationEnabled() {
        return Boolean.parseBoolean(getProperty("cache.validation.enabled", "true"));
    }
    
    public boolean isAutoHealingEnabled() {
        return Boolean.parseBoolean(getProperty("cache.autohealing.enabled", "false"));
    }
    
    
    // Reporting Configuration
    public boolean isReportingEnabled() {
        return Boolean.parseBoolean(getProperty("reporting.enabled", "true"));
    }
    
    public boolean isScreenshotsEnabled() {
        return Boolean.parseBoolean(getProperty("reporting.screenshots.enabled", "true"));
    }
    
    public boolean isScreenshotsOnFailure() {
        return Boolean.parseBoolean(getProperty("reporting.screenshots.onfailure", "true"));
    }
    
    public boolean isAgentDecisionsLogged() {
        return Boolean.parseBoolean(getProperty("reporting.agent.decisions", "true"));
    }
    
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
