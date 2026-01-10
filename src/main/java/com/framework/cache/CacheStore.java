package com.framework.cache;

import com.framework.config.ExecutionConfig;
import com.framework.models.PageCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages reading and writing of page-wise locator cache
 * Cache files are stored in JSON format: cache/pages/{PageName}.json
 */
public class CacheStore {
    private static final Logger logger = LoggerFactory.getLogger(CacheStore.class);
    private static CacheStore instance;
    
    private final String cacheDirectory;
    private final Gson gson;
    private final Map<String, PageCache> memoryCache;
    
    private CacheStore() {
        this.cacheDirectory = ExecutionConfig.getInstance().getCacheDirectory();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.memoryCache = new HashMap<>();
        ensureCacheDirectoryExists();
    }
    
    public static CacheStore getInstance() {
        if (instance == null) {
            synchronized (CacheStore.class) {
                if (instance == null) {
                    instance = new CacheStore();
                }
            }
        }
        return instance;
    }
    
    private void ensureCacheDirectoryExists() {
        try {
            Path path = Paths.get(cacheDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Cache directory created: " + cacheDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create cache directory: " + e.getMessage());
        }
    }
    
    /**
     * Load page cache from file
     */
    public PageCache loadPageCache(String pageName) {
        // Check memory cache first
        if (memoryCache.containsKey(pageName)) {
            logger.debug("Loading page cache from memory: " + pageName);
            return memoryCache.get(pageName);
        }
        
        // Load from file
        String filename = getFilename(pageName);
        File file = new File(filename);
        
        if (!file.exists()) {
            logger.debug("No cache file found for page: " + pageName);
            return null;
        }
        
        try (FileReader reader = new FileReader(file)) {
            PageCache pageCache = gson.fromJson(reader, PageCache.class);
            memoryCache.put(pageName, pageCache);
            logger.info("Loaded cache for page: " + pageName);
            return pageCache;
        } catch (IOException e) {
            logger.error("Failed to load cache for page " + pageName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Save page cache to file
     */
    public void savePageCache(PageCache pageCache) {
        String pageName = pageCache.getPage();
        String filename = getFilename(pageName);
        
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(pageCache, writer);
            memoryCache.put(pageName, pageCache);
            logger.info("Saved cache for page: " + pageName);
        } catch (IOException e) {
            logger.error("Failed to save cache for page " + pageName + ": " + e.getMessage());
        }
    }
    
    /**
     * Get locator for a specific element on a page
     */
    public String getLocator(String pageName, String elementName) {
        PageCache pageCache = loadPageCache(pageName);
        if (pageCache == null) {
            return null;
        }
        return pageCache.getLocator(elementName);
    }
    
    /**
     * Add or update a locator for a specific element on a page
     */
    public void addLocator(String pageName, String elementName, String locator) {
        PageCache pageCache = loadPageCache(pageName);
        if (pageCache == null) {
            pageCache = new PageCache(pageName);
        }
        
        pageCache.addLocator(elementName, locator);
        savePageCache(pageCache);
        logger.info("Added locator for " + pageName + "." + elementName);
    }
    
    /**
     * Check if locator exists in cache
     */
    public boolean hasLocator(String pageName, String elementName) {
        PageCache pageCache = loadPageCache(pageName);
        return pageCache != null && pageCache.hasLocator(elementName);
    }
    
    /**
     * Clear memory cache
     */
    public void clearMemoryCache() {
        memoryCache.clear();
        logger.info("Memory cache cleared");
    }
    
    /**
     * Delete cache file for a specific page
     */
    public void deletePageCache(String pageName) {
        String filename = getFilename(pageName);
        File file = new File(filename);
        if (file.exists()) {
            if (file.delete()) {
                memoryCache.remove(pageName);
                logger.info("Deleted cache for page: " + pageName);
            }
        }
    }
    
    private String getFilename(String pageName) {
        return cacheDirectory + File.separator + pageName + ".json";
    }
}
