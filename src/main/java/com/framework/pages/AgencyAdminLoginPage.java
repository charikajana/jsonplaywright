package com.framework.pages;

import com.framework.config.EnvironmentConfig;
import com.microsoft.playwright.Locator;

/**
 * Page Object for Agency Admin Login Page
 */
public class AgencyAdminLoginPage extends BasePage {
    
    // ==================== LOCATORS ====================
    private static final String USERNAME_INPUT = "#txtUserName";
    private static final String PASSWORD_INPUT = "#txtPassword";
    private static final String LOGIN_BUTTON = "#btnLogin";
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
     * Enter username in the login form
     */
    public AgencyAdminLoginPage enterUsername(String username) {
        logger.info("Entering Agency Admin username: {}", username);
        getUsernameInput().fill(username);
        return this;
    }
    
    /**
     * Enter password in the login form
     */
    public AgencyAdminLoginPage enterPassword(String password) {
        logger.info("Entering Agency Admin password");
        getPasswordInput().fill(password);
        return this;
    }
    
    /**
     * Click login button
     */
    public void clickLogin() {
        logger.info("Clicking Agency Admin Login button");
        getLoginButton().click();
        waitForPageLoad();
    }
    
    /**
     * Enter credentials from environment properties
     */
    public AgencyAdminLoginPage enterCredentialsFromEnv() {
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
