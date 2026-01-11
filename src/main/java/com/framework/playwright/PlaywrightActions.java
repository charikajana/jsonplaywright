package com.framework.playwright;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for traditional step definitions.
 * Provides easy access to the Playwright Page and common automation utilities.
 */
public abstract class PlaywrightActions {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Get the Playwright Page instance for the current thread.
     * @return Playwright Page
     */
    protected Page getPage() {
        return PlaywrightManager.getInstance().getPage();
    }

    /**
     * Shortcut to the page object for cleaner code in subclasses.
     * Usage: page().click("button");
     */
    protected Page page() {
        return getPage();
    }

    /**
     * Navigate to a URL with logging.
     */
    protected void navigate(String url) {
        logger.info("[PLAYWRIGHT] Navigating to: {}", url);
        page().navigate(url);
    }

    /**
     * Click an element with logging.
     */
    protected void click(String selector) {
        logger.info("[PLAYWRIGHT] Clicking: {}", selector);
        page().click(selector);
    }

    /**
     * Double click an element with logging.
     */
    protected void doubleClick(String selector) {
        logger.info("[PLAYWRIGHT] Double-clicking: {}", selector);
        page().dblclick(selector);
    }

    /**
     * Right click an element with logging.
     */
    protected void rightClick(String selector) {
        logger.info("[PLAYWRIGHT] Right-clicking: {}", selector);
        page().click(selector, new Page.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
    }

    /**
     * Type into an element (using fill) with logging.
     */
    protected void type(String selector, String text) {
        logger.info("[PLAYWRIGHT] Typing into {}: {}", selector, text);
        page().fill(selector, text);
    }

    /**
     * Clear an input field with logging.
     */
    protected void clear(String selector) {
        logger.info("[PLAYWRIGHT] Clearing field: {}", selector);
        page().fill(selector, "");
    }

    /**
     * Check a checkbox or radio button with logging.
     */
    protected void check(String selector) {
        logger.info("[PLAYWRIGHT] Checking: {}", selector);
        page().check(selector);
    }

    /**
     * Uncheck a checkbox with logging.
     */
    protected void uncheck(String selector) {
        logger.info("[PLAYWRIGHT] Unchecking: {}", selector);
        page().uncheck(selector);
    }

    /**
     * Select an option from a dropdown by label/value with logging.
     */
    protected void selectOption(String selector, String value) {
        logger.info("[PLAYWRIGHT] Selecting '{}' from: {}", value, selector);
        page().selectOption(selector, value);
    }

    /**
     * Hover over an element with logging.
     */
    protected void hover(String selector) {
        logger.info("[PLAYWRIGHT] Hovering over: {}", selector);
        page().hover(selector);
    }

    /**
     * Press a keyboard key with logging.
     */
    protected void pressKey(String key) {
        logger.info("[PLAYWRIGHT] Pressing key: {}", key);
        page().keyboard().press(key);
    }

    /**
     * Press a keyboard key on a specific element.
     */
    protected void pressKey(String selector, String key) {
        logger.info("[PLAYWRIGHT] Pressing key '{}' on: {}", key, selector);
        page().press(selector, key);
    }

    /**
     * Drag one element to another.
     */
    protected void dragAndDrop(String sourceSelector, String targetSelector) {
        logger.info("[PLAYWRIGHT] Dragging from {} to {}", sourceSelector, targetSelector);
        page().dragAndDrop(sourceSelector, targetSelector);
    }

    /**
     * Scroll to an element.
     */
    protected void scrollTo(String selector) {
        logger.info("[PLAYWRIGHT] Scrolling to: {}", selector);
        page().locator(selector).scrollIntoViewIfNeeded();
    }

    /**
     * Wait for an element to be visible.
     */
    protected void waitForVisible(String selector) {
        logger.info("[PLAYWRIGHT] Waiting for visibility of: {}", selector);
        page().waitForSelector(selector, new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
    }

    /**
     * Wait for an element to be hidden.
     */
    protected void waitForHidden(String selector) {
        logger.info("[PLAYWRIGHT] Waiting for hidden state of: {}", selector);
        page().waitForSelector(selector, new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    /**
     * Check if an element is visible (returns boolean).
     */
    protected boolean isVisible(String selector) {
        return page().isVisible(selector);
    }

    /**
     * Get text content of an element.
     */
    protected String getText(String selector) {
        return page().textContent(selector);
    }

    /**
     * Get attribute value of an element.
     */
    protected String getAttribute(String selector, String attribute) {
        return page().getAttribute(selector, attribute);
    }

    /**
     * Refresh the current page.
     */
    protected void reload() {
        logger.info("[PLAYWRIGHT] Reloading page");
        page().reload();
    }

    /**
     * Go back to the previous page.
     */
    protected void goBack() {
        logger.info("[PLAYWRIGHT] Going back");
        page().goBack();
    }

    /**
     * Go forward to the next page.
     */
    protected void goForward() {
        logger.info("[PLAYWRIGHT] Going forward");
        page().goForward();
    }

    /**
     * Take a screenshot, save it to a file, and attach it to the Allure report.
     */
    protected void screenshot(String path) {
        logger.info("[PLAYWRIGHT] Taking screenshot to: {}", path);
        byte[] screenshot = page().screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get(path)));
        Allure.addAttachment("Step Screenshot", new java.io.ByteArrayInputStream(screenshot));
    }

    /**
     * Take a screenshot and attach it directly to the Allure report.
     */
    protected void screenshot() {
        logger.info("[PLAYWRIGHT] Taking screenshot for Allure report");
        byte[] screenshot = page().screenshot(new Page.ScreenshotOptions().setFullPage(true));
        Allure.addAttachment("Step Screenshot", new java.io.ByteArrayInputStream(screenshot));
    }

    /**
     * Handle JS dialogs (Alerts/Confirms) by accepting them.
     */
    protected void acceptDialog() {
        page().onceDialog(com.microsoft.playwright.Dialog::accept);
    }

    /**
     * Handle JS dialogs by dismissing them.
     */
    protected void dismissDialog() {
        page().onceDialog(com.microsoft.playwright.Dialog::dismiss);
    }

    /**
     * Get current page title.
     */
    protected String getTitle() {
        return page().title();
    }

    /**
     * Get current page URL.
     */
    protected String getUrl() {
        return page().url();
    }
}
