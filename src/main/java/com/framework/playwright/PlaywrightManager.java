package com.framework.playwright;

import com.framework.config.ExecutionConfig;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Playwright browser lifecycle with thread-safe context isolation.
 * Supports parallel test execution with isolated browser contexts per thread.
 */
public class PlaywrightManager {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightManager.class);
    
    // Singleton instance
    private static PlaywrightManager instance;
    
    // Thread-local instances for full isolation
    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();
    
    private final ExecutionConfig config;
    
    private PlaywrightManager() {
        this.config = ExecutionConfig.getInstance();
    }
    
    public static synchronized PlaywrightManager getInstance() {
        if (instance == null) {
            instance = new PlaywrightManager();
        }
        return instance;
    }
    
    /**
     * Initializes browser for the current thread.
     * Creates thread-local playwright, browser, context and page for isolation.
     */
    public void initializeBrowser() {
        // Initialize Playwright for THIS thread
        if (playwrightThreadLocal.get() == null) {
            logger.info("[BROWSER] Initializing Playwright for thread: {}", Thread.currentThread().getName());
            Playwright playwright = Playwright.create();
            playwrightThreadLocal.set(playwright);
            
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(config.isBrowserHeadless())
                    .setSlowMo(config.getSlowMo());
            
            Browser browser = getBrowserType(playwright).launch(launchOptions);
            browserThreadLocal.set(browser);
            logger.info("[BROWSER] Browser launched for thread: {}", Thread.currentThread().getName());
        }
        
        // Create thread-local context and page
        if (contextThreadLocal.get() == null) {
            Browser browser = browserThreadLocal.get();
            logger.info("[BROWSER] Creating new context for thread: {}", Thread.currentThread().getName());
            
            String videoDir = System.getProperty("video.path", "reports/videos");
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1570, 780)
                .setAcceptDownloads(true)
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setRecordVideoDir(java.nio.file.Paths.get(videoDir)));
            
            contextThreadLocal.set(context);
            
            Page page = context.newPage();
            page.setDefaultTimeout(config.getBrowserTimeout());
            pageThreadLocal.set(page);
            
            logger.info("[BROWSER] Thread-local context and page created");
        }
    }
    
    /**
     * Gets the page for the current thread.
     */
    public Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            initializeBrowser();
            page = pageThreadLocal.get();
        }
        return page;
    }
    
    /**
     * Gets the browser context for the current thread.
     */
    public BrowserContext getContext() {
        return contextThreadLocal.get();
    }
    
    /**
     * Takes a screenshot of the current page.
     */
    public byte[] takeScreenshot() {
        try {
            Page page = getPage();
            if (page != null && !page.isClosed()) {
                try {
                    // Try full page first
                    return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                } catch (Exception e) {
                    logger.warn("[BROWSER] Full page screenshot failed, trying viewport: {}", e.getMessage());
                    // Fallback to viewport only
                    return page.screenshot(new Page.ScreenshotOptions().setFullPage(false));
                }
            }
        } catch (Exception e) {
            logger.error("[BROWSER] Failed to take screenshot: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Gets the video path for the current page.
     */
    public java.nio.file.Path getVideoPath() {
        try {
            Page page = pageThreadLocal.get();
            if (page != null && page.video() != null) {
                return page.video().path();
            }
        } catch (Exception e) {
            logger.error("[BROWSER] Failed to get video path: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Closes everything for the current thread.
     */
    public void closeBrowser() {
        try {
            Page page = pageThreadLocal.get();
            if (page != null) {
                page.close();
                pageThreadLocal.remove();
            }
            
            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                context.close();
                contextThreadLocal.remove();
            }
            
            Browser browser = browserThreadLocal.get();
            if (browser != null) {
                browser.close();
                browserThreadLocal.remove();
            }
            
            Playwright playwright = playwrightThreadLocal.get();
            if (playwright != null) {
                playwright.close();
                playwrightThreadLocal.remove();
            }
            
            logger.info("[BROWSER] Browser closed for thread: {}", Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("[BROWSER] Error closing browser for thread: {}", e.getMessage());
        }
    }
    
    /**
     * Closes all browser instances if any are left.
     */
    public static synchronized void closeAll() {
        instance = null;
    }
    
    /**
     * Gets the browser type based on configuration.
     */
    private BrowserType getBrowserType(Playwright playwright) {
        String browserName = config.getBrowserType().toLowerCase();
        
        switch (browserName) {
            case "firefox":
                return playwright.firefox();
            case "webkit":
            case "safari":
                return playwright.webkit();
            case "chrome":
            case "chromium":
            default:
                return playwright.chromium();
        }
    }
    
    /**
     * Creates a new page in the current context.
     * Useful for multi-tab scenarios.
     * 
     * @return New Page object
     */
    public Page newPage() {
        BrowserContext context = getContext();
        if (context != null) {
            Page newPage = context.newPage();
            newPage.setDefaultTimeout(config.getBrowserTimeout());
            return newPage;
        }
        return null;
    }
    
    /**
     * Gets all pages in the current context.
     * 
     * @return List of all pages
     */
    public java.util.List<Page> getAllPages() {
        BrowserContext context = getContext();
        if (context != null) {
            return context.pages();
        }
        return java.util.List.of();
    }
}
