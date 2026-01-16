package com.framework.pages;

import com.framework.config.EnvironmentConfig;
import com.microsoft.playwright.Locator;

/**
 * Page Object for HotelBooker Login Page
 */
public class HotelBookerLoginPage extends BasePage {
    
    // ==================== LOCATORS ====================
    private static final String USERNAME_INPUT = "#ctl00_cphMainContent_txtUserName";
    private static final String PASSWORD_INPUT = "#ctl00_cphMainContent_txtPassword";
    private static final String LOGIN_BUTTON = "#ctl00_cphMainContent_btnLogin";
    private static final String FORGOTTEN_PASSWORD_LINK = "a:has-text('Forgotten Password?')";
    
    // ==================== ELEMENT GETTERS ====================
    public Locator getUsernameInput() {
        return locator(USERNAME_INPUT);
    }
    
    public Locator getPasswordInput() {
        return locator(PASSWORD_INPUT);
    }
    
    public Locator getLoginButton() {
        return locator(LOGIN_BUTTON);
    }
    
    public Locator getForgottenPasswordLink() {
        return locator(FORGOTTEN_PASSWORD_LINK);
    }
    
    // ==================== PAGE ACTIONS ====================
    
    /**
     * Navigate to HotelBooker application using environment BaseURL
     */
    public HotelBookerLoginPage navigateToHotelBooker() {
        String baseUrl = EnvironmentConfig.getProperty("BaseURL");
        logger.info("Navigating to HotelBooker: {}", baseUrl);
        navigateTo(baseUrl);
        return this;
    }
    
    /**
     * Enter username in the login form
     */
    public HotelBookerLoginPage enterUsername(String username) {
        logger.info("Entering username: {}", username);
        getUsernameInput().fill(username);
        return this;
    }
    
    /**
     * Enter password in the login form
     */
    public HotelBookerLoginPage enterPassword(String password) {
        logger.info("Entering password");
        getPasswordInput().fill(password);
        return this;
    }
    
    /**
     * Click login button
     */
    public void clickLogin() {
        logger.info("Clicking Login button");
        getLoginButton().click();
        waitForPageLoad();
    }
    
    /**
     * Enter credentials from environment properties
     */
    public HotelBookerLoginPage enterCredentialsFromEnv() {
        String username = EnvironmentConfig.getProperty("Username");
        String password = EnvironmentConfig.getProperty("Password");
        
        enterUsername(username);
        enterPassword(password);
        return this;
    }
    
    /**
     * Complete login flow with environment credentials
     */
    public void loginWithEnvCredentials() {
        enterCredentialsFromEnv();
        clickLogin();
    }
    
    /**
     * Login with specific credentials
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
    
    /**
     * Check if login page is displayed
     */
    public boolean isDisplayed() {
        return getUsernameInput().isVisible() && getLoginButton().isVisible();
    }
}
