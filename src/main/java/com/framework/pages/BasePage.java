package com.framework.pages;

import com.framework.playwright.PlaywrightManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Page class providing common functionality for all Page Objects.
 */
public abstract class BasePage {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Get the current Playwright page instance
     */
    protected Page page() {
        return PlaywrightManager.getInstance().getPage();
    }
    
    /**
     * Find element by CSS selector
     */
    protected Locator locator(String selector) {
        return page().locator(selector);
    }
    
    /**
     * Find element by ID
     */
    protected Locator byId(String id) {
        return page().locator("#" + id);
    }
    
    /**
     * Find element by text
     */
    protected Locator byText(String text) {
        return page().getByText(text);
    }
    
    /**
     * Find element by role and name
     */
    protected Locator byRole(String role, String name) {
        return page().getByRole(com.microsoft.playwright.options.AriaRole.valueOf(role.toUpperCase()), 
                new Page.GetByRoleOptions().setName(name));
    }
    
    /**
     * Wait for page to fully load
     */
    protected void waitForPageLoad() {
        page().waitForLoadState(LoadState.NETWORKIDLE);
    }
    
    /**
     * Wait for page DOM to be ready
     */
    protected void waitForDomReady() {
        page().waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
    
    /**
     * Navigate to URL and wait for complete page load
     */
    protected void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        page().navigate(url);
        waitForPageLoad();
    }
    
    /**
     * Get page title
     */
    protected String getTitle() {
        return page().title();
    }
    
    /**
     * Check if element is visible
     */
    protected boolean isVisible(String selector) {
        return locator(selector).isVisible();
    }
}
