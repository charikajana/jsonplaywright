package com.framework.steps;

import com.framework.config.EnvironmentConfig;
import com.framework.pages.HotelBookerLocators;
import com.framework.playwright.PlaywrightActions;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/**
 * Traditional step definitions extending PlaywrightActions.
 * These steps use environment-based configuration from env/*.properties files.
 * Locators are defined in HotelBookerLocators (POM pattern).
 * 
 * For JSON-driven steps, see UniversalStepDefinition.java and the 
 * locatorRepository JSON files.
 */
public class TraditionalSteps extends PlaywrightActions {

    @Given("Open Browser and Navigate to HotelBooker")
    public void openBrowserAndNavigateToHotelBooker() {
        String baseUrl = EnvironmentConfig.getProperty("BaseURL", "https://hotelbooker.cert.sabre.com/");
        logger.info("Navigating to HotelBooker: {}", baseUrl);
        navigate(baseUrl);
        // Wait for page to fully load
        page().waitForLoadState(LoadState.NETWORKIDLE);
        page().waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @When("user enters username and password")
    public void userEntersUsernameAndPassword() {
        String username = EnvironmentConfig.getProperty("Username");
        String password = EnvironmentConfig.getProperty("Password");
        
        logger.info("Entering credentials for user: {}", username);
        // Wait for login form to be visible
        page().waitForSelector(HotelBookerLocators.USERNAME_FIELD, 
            new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(30000));
        type(HotelBookerLocators.USERNAME_FIELD, username);
        type(HotelBookerLocators.PASSWORD_FIELD, password);
    }

    @When("Enter username and password click on Login button")
    public void enterUsernameAndPasswordClickOnLoginButtonAgencyAdmin() {
        String username = EnvironmentConfig.getProperty("Username");
        String password = EnvironmentConfig.getProperty("Password");
        
        logger.info("Logging into Agency Admin with user: {}", username);
        // Wait for login form to be visible
        page().waitForSelector(HotelBookerLocators.ADMIN_USERNAME_FIELD,
            new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(30000));
        type(HotelBookerLocators.ADMIN_USERNAME_FIELD, username);
        type(HotelBookerLocators.ADMIN_PASSWORD_FIELD, password);
        click(HotelBookerLocators.ADMIN_LOGIN_BUTTON);
    }

}
