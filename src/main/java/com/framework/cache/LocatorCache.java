package com.framework.cache;

import com.framework.models.PageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level interface for locator cache operations
 * Combines CacheStore and CacheValidator
 */
public class LocatorCache {
    private static final Logger logger = LoggerFactory.getLogger(LocatorCache.class);
    private static LocatorCache instance;
    
    private final CacheStore cacheStore;
    
    private LocatorCache() {
        this.cacheStore = CacheStore.getInstance();
    }
    
    public static LocatorCache getInstance() {
        if (instance == null) {
            synchronized (LocatorCache.class) {
                if (instance == null) {
                    instance = new LocatorCache();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get locator from cache with validation
     */
    public String getLocator(String pageName, String elementName) {
        PageCache pageCache = cacheStore.loadPageCache(pageName);
        
        if (pageCache == null) {
            logger.debug("No cache found for page: " + pageName);
            return null;
        }
        
        // Validate page identity
        if (!CacheValidator.validatePageIdentity(pageCache)) {
            logger.warn("Page identity validation failed for: " + pageName);
            return null;
        }
        
        String locator = pageCache.getLocator(elementName);
        if (locator == null) {
            logger.debug("No locator found for element: " + elementName);
            return null;
        }
        
        logger.info("Retrieved cached locator for " + pageName + "." + elementName);
        return locator;
    }
    
    /**
     * Add locator to cache with page identity
     */
    public void addLocator(String pageName, String elementName, String locator) {
        PageCache pageCache = cacheStore.loadPageCache(pageName);
        
        if (pageCache == null) {
            pageCache = new PageCache(pageName);
            // Create and set page identity
            PageCache.PageIdentity identity = CacheValidator.createPageIdentity();
            pageCache.setIdentity(identity);
            logger.info("Created new page cache for: " + pageName);
        }
        
        pageCache.addLocator(elementName, locator);
        cacheStore.savePageCache(pageCache);
        
        logger.info("Cached locator for " + pageName + "." + elementName + ": " + locator);
    }
    
    /**
     * Check if locator exists in cache
     */
    public boolean hasLocator(String pageName, String elementName) {
        return cacheStore.hasLocator(pageName, elementName);
    }
    
    /**
     * Clear all caches
     */
    public void clearCache() {
        cacheStore.clearMemoryCache();
        logger.info("All caches cleared");
    }
    
    /**
     * Delete cache for specific page
     */
    public void deletePageCache(String pageName) {
        cacheStore.deletePageCache(pageName);
    }
}
