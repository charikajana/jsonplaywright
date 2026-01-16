package com.framework.steps;

import com.framework.pages.AgencyAdminLoginPage;
import com.framework.pages.HotelBookerLoginPage;
import com.framework.playwright.PlaywrightActions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

/**
 * Traditional step definitions using Page Object Model (POM) pattern.
 * This class delegates actions to Page Object classes for better maintainability.
 */
public class TraditionalSteps extends PlaywrightActions {

    // Page Objects
    private final HotelBookerLoginPage hotelBookerLoginPage = new HotelBookerLoginPage();
    private final AgencyAdminLoginPage agencyAdminLoginPage = new AgencyAdminLoginPage();

    @Given("Open Browser and Navigate to HotelBooker")
    public void openBrowserAndNavigateToHotelBooker() {
        hotelBookerLoginPage.navigateToHotelBooker();
    }

    @When("user enters username and password")
    public void userEntersUsernameAndPassword() {
        hotelBookerLoginPage.enterCredentialsFromEnv();
    }

    @When("Enter username and password click on Login button")
    public void enterUsernameAndPasswordClickOnLoginButtonAgencyAdmin() {
        agencyAdminLoginPage.loginWithEnvCredentials();
    }

}
