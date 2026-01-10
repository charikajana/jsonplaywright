package com.framework.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents cached locators for a specific page
 */
public class PageCache {
    private String page;
    private PageIdentity identity;
    private Map<String, String> locators;
    
    public PageCache() {
        this.locators = new HashMap<>();
    }
    
    public PageCache(String page) {
        this.page = page;
        this.locators = new HashMap<>();
    }
    
    // Getters and Setters
    public String getPage() {
        return page;
    }
    
    public void setPage(String page) {
        this.page = page;
    }
    
    public PageIdentity getIdentity() {
        return identity;
    }
    
    public void setIdentity(PageIdentity identity) {
        this.identity = identity;
    }
    
    public Map<String, String> getLocators() {
        return locators;
    }
    
    public void setLocators(Map<String, String> locators) {
        this.locators = locators;
    }
    
    public void addLocator(String elementName, String locator) {
        this.locators.put(elementName, locator);
    }
    
    public String getLocator(String elementName) {
        return this.locators.get(elementName);
    }
    
    public boolean hasLocator(String elementName) {
        return this.locators.containsKey(elementName);
    }
    
    /**
     * Page identity information for validation
     */
    public static class PageIdentity {
        private String urlPattern;
        private String title;
        
        public PageIdentity() {}
        
        public PageIdentity(String urlPattern, String title) {
            this.urlPattern = urlPattern;
            this.title = title;
        }
        
        public String getUrlPattern() {
            return urlPattern;
        }
        
        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
}
