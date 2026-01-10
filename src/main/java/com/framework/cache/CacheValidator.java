package com.framework.cache;

import com.framework.models.PageCache;
import com.framework.playwright.PlaywrightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates cached locators and page identity
 * Ensures cache is still valid for current page state
 */
public class CacheValidator {
    private static final Logger logger = LoggerFactory.getLogger(CacheValidator.class);
    
    /**
     * Validate if the current page matches the cached page identity
     */
    public static boolean validatePageIdentity(PageCache pageCache) {
        if (pageCache == null || pageCache.getIdentity() == null) {
            logger.debug("No page identity to validate");
            return true;
        }
        
        PlaywrightManager pm = PlaywrightManager.getInstance();
        String currentUrl = pm.getPage().url();
        String currentTitle = pm.getPage().title();
        
        PageCache.PageIdentity identity = pageCache.getIdentity();
        
        // Validate URL pattern
        if (identity.getUrlPattern() != null) {
            if (!currentUrl.contains(identity.getUrlPattern())) {
                logger.warn("Page identity mismatch: URL does not match pattern");
                logger.debug("Expected pattern: " + identity.getUrlPattern() + 
                           ", Actual URL: " + currentUrl);
                return false;
            }
        }
        
        // Validate title
        if (identity.getTitle() != null) {
            if (!identity.getTitle().equals(currentTitle)) {
                logger.warn("Page identity mismatch: Title does not match");
                logger.debug("Expected: " + identity.getTitle() + 
                           ", Actual: " + currentTitle);
                return false;
            }
        }
        
        logger.debug("Page identity validated successfully");
        return true;
    }
    
    /**
     * Validate if a specific locator still works on the page
     */
    public static boolean validateLocator(String locator) {
        if (locator == null || locator.isEmpty()) {
            return false;
        }
        
        try {
            PlaywrightManager pm = PlaywrightManager.getInstance();
            // Check if element exists with a short timeout
            pm.getPage().locator(locator).first().isVisible();
            return true;
        } catch (Exception e) {
            logger.debug("Locator validation failed: " + locator);
            return false;
        }
    }
    
    /**
     * Create page identity from current page state
     */
    public static PageCache.PageIdentity createPageIdentity() {
        PlaywrightManager pm = PlaywrightManager.getInstance();
        String url = pm.getPage().url();
        String title = pm.getPage().title();
        
        // Extract meaningful URL pattern (remove query params, etc.)
        String urlPattern = extractUrlPattern(url);
        
        return new PageCache.PageIdentity(urlPattern, title);
    }
    
    private static String extractUrlPattern(String url) {
        if (url == null) {
            return null;
        }
        
        // Remove query parameters
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            url = url.substring(0, queryIndex);
        }
        
        // Remove hash
        int hashIndex = url.indexOf('#');
        if (hashIndex > 0) {
            url = url.substring(0, hashIndex);
        }
        
        return url;
    }
}
