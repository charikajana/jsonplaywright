package com.framework.utils;

import com.framework.config.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * URL Resolver - Handles environment-specific URL resolution
 * Converts parameterized URLs to actual URLs based on current environment
 */
public class UrlResolver {
    private static final Logger logger = LoggerFactory.getLogger(UrlResolver.class);
    
    /**
     * Resolve URL with environment-specific values
     * 
     * @param url URL that may contain placeholders like ${BASE_URL}
     * @return Resolved URL for current environment
     */
    public static String resolve(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // Check if URL has placeholders
        if (!url.contains("${")) {
            // No placeholders, return as-is
            return url;
        }
        
        String resolved = EnvironmentConfig.resolveUrl(url);
        logger.debug("URL resolved: {} -> {}", url, resolved);
        return resolved;
    }
    
    /**
     * Resolve multiple URLs
     */
    public static String[] resolve(String... urls) {
        String[] resolved = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            resolved[i] = resolve(urls[i]);
        }
        return resolved;
    }
    
    /**
     * Convert absolute URL to parameterized URL
     * Useful when saving steps collected from a specific environment
     * 
     * @param absoluteUrl Absolute URL like https://dev.saucedemo.com/inventory.html
     * @return Parameterized URL like ${BASE_URL}/inventory.html
     */
    public static String parameterize(String absoluteUrl) {
        if (absoluteUrl == null || absoluteUrl.isEmpty()) {
            return absoluteUrl;
        }
        
        String parameterized = EnvironmentConfig.parameterizeUrl(absoluteUrl);
        
        if (!parameterized.equals(absoluteUrl)) {
            logger.debug("URL parameterized: {} -> {}", absoluteUrl, parameterized);
        }
        
        return parameterized;
    }
    
    /**
     * Parameterize multiple URLs
     */
    public static String[] parameterize(String... urls) {
        String[] parameterized = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            parameterized[i] = parameterize(urls[i]);
        }
        return parameterized;
    }
    
    /**
     * Check if URL needs resolution (contains placeholders)
     */
    public static boolean needsResolution(String url) {
        return url != null && url.contains("${");
    }
    
    /**
     * Build URL by appending path to base URL
     * 
     * @param path Path to append (e.g., "/inventory.html")
     * @return Full URL like ${BASE_URL}/inventory.html or actual URL if environment is set
     */
    public static String buildUrl(String path) {
        if (path == null || path.isEmpty()) {
            return EnvironmentConfig.getBaseUrl();
        }
        
        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return EnvironmentConfig.getBaseUrl() + path;
    }
    
    /**
     * Build parameterized URL
     */
    public static String buildParameterizedUrl(String path) {
        if (path == null || path.isEmpty()) {
            return "${BASE_URL}";
        }
        
        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return "${BASE_URL}" + path;
    }
}
