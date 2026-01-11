package com.framework.steps;

import com.framework.playwright.PlaywrightActions;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

/**
 * Example of traditional coded step definitions, now extending PlaywrightActions.
 * This allows direct access to the 'page()' method and other utilities.
 */
public class TraditionalSteps extends PlaywrightActions {

    @Given("Open custom URL {string}")
    public void openCustomUrl(String url) {
        // We can now use the 'navigate' method from PlaywrightActions or direct 'page()'
        navigate(url);
    }

    @Then("Verify page title contains {string}")
    public void verifyTitle(String expectedTitle) {
        String actualTitle = page().title();
        logger.info("[TRADITIONAL] Verifying title contains: {}", expectedTitle);
        
        if (!actualTitle.contains(expectedTitle)) {
            throw new AssertionError("Expected title to contain '" + expectedTitle + "' but was '" + actualTitle + "'");
        }
    }
}
