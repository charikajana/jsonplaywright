package com.framework.steps;

import com.framework.pages.HotelSearchResultsPage;
import com.framework.playwright.PlaywrightActions;
import com.microsoft.playwright.Locator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.util.List;

/**
 * Traditional Step Definitions for Hotel Rate Selection.
 * Handles complex multi-level looping: Pages → Hotels → Rates
 * 
 * This step searches across all pages and hotels to find a rate
 * matching the requested provider and refundable criteria.
 */
public class HotelRateSelectionSteps extends PlaywrightActions {

    private HotelSearchResultsPage hotelPage;
    
    /**
     * Main step: Select a rate plan matching provider and refundable criteria
     * 
     * This step performs a comprehensive search:
     * 1. Loop through all pagination pages
     * 2. For each page, loop through all hotels
     * 3. Check if hotel's Active Content Provider matches
     * 4. Click Check Availability and loop through rates
     * 5. Check rate provider and refundable status
     * 6. Select the first matching rate
     * 
     * @param provider The content provider to match (e.g., "Sabre", "Amadeus")
     * @param refundable The refundable status to match ("Yes" or "No")
     */
    @And("Select the Rate Plan from {string} with refundable {string}")
    @When("Select the Rate Plan from {string} with refundable {string}")
    @Step("Select Rate Plan from provider '{0}' with refundable '{1}'")
    public void selectRatePlanFromProviderWithRefundable(String provider, String refundable) {
        logger.info("===================================================================");
        logger.info("STARTING RATE SELECTION SEARCH");
        logger.info("   Provider: {} | Refundable: {}", provider, refundable);
        logger.info("===================================================================");
        
        hotelPage = new HotelSearchResultsPage(page());
        
        int totalPages = hotelPage.getPageCount();
        logger.info("Total pages to search: {}", totalPages);
        
        boolean matchFound = false;
        String matchDetails = "";
        
        // OUTER LOOP: Iterate through pages
        for (int pageNum = 1; pageNum <= totalPages && !matchFound; pageNum++) {
            logger.info("-------------------------------------------------------------------");
            logger.info("SEARCHING PAGE {} of {}", pageNum, totalPages);
            logger.info("-------------------------------------------------------------------");
            
            // Navigate to page (skip for page 1 if already there)
            if (pageNum > 1) {
                hotelPage.goToPage(pageNum);
            }
            
            // Get hotels on current page
            List<Locator> hotels = hotelPage.getAllHotels();
            int hotelCount = hotels.size();
            logger.info("Found {} hotels on page {}", hotelCount, pageNum);
            
            // MIDDLE LOOP: Iterate through hotels on current page
            for (int hotelIndex = 0; hotelIndex < hotelCount && !matchFound; hotelIndex++) {
                String hotelName = hotelPage.getHotelName(hotelIndex);
                logger.info("-------------------------------------------------------------");
                logger.info("HOTEL {}: {}", hotelIndex + 1, hotelName);
                
                // Check hotel's Active Content Provider
                String hotelProvider = hotelPage.getHotelActiveProvider(hotelIndex);
                logger.info("   Active Provider: {}", hotelProvider);
                
                // Check if provider matches at hotel level
                if (!providerMatches(hotelProvider, provider)) {
                    logger.info("   Provider mismatch - Skipping hotel");
                    logger.info("-------------------------------------------------------------");
                    continue;
                }
                
                logger.info("   Provider matches at hotel level");
                
                // Click Check Availability
                if (!hotelPage.clickCheckAvailability(hotelIndex)) {
                    logger.info("   Could not click Check Availability - Skipping");
                    logger.info("-------------------------------------------------------------");
                    continue;
                }
                
                // Check if rates are available
                if (!hotelPage.hasRatesAvailable(hotelIndex)) {
                    logger.info("   No online availability - Skipping");
                    hotelPage.hideAvailability(hotelIndex);
                    logger.info("-------------------------------------------------------------");
                    continue;
                }
                
                // Get rate rows
                Locator rateRows = hotelPage.getRateRows(hotelIndex);
                int rateCount = rateRows.count();
                logger.info("   Found {} rates", rateCount);
                
                // INNER LOOP: Iterate through rates
                for (int rateIndex = 0; rateIndex < rateCount && !matchFound; rateIndex++) {
                    logger.info("   Rate {}: Checking...", rateIndex + 1);
                    
                    // Get rate provider
                    String rateProvider = hotelPage.getRateProvider(rateRows.nth(rateIndex));
                    logger.info("      Provider: {}", rateProvider);
                    
                    // Check if rate provider matches
                    if (!providerMatches(rateProvider, provider)) {
                        logger.info("      Provider mismatch");
                        continue;
                    }
                    
                    logger.info("      Provider matches");
                    
                    // Click Full Rate Information to check refundable status
                    if (!hotelPage.clickFullRateInfo(rateIndex)) {
                        logger.info("      Could not expand rate info");
                        continue;
                    }
                    
                    // Get cancellation policy and determine refundable status
                    String cancellationPolicy = hotelPage.getCancellationPolicy(rateIndex);
                    String rateRefundable = hotelPage.isRefundable(cancellationPolicy);
                    logger.info("      Cancellation: {}", cancellationPolicy);
                    logger.info("      Refundable: {}", rateRefundable);
                    
                    // Check if refundable status matches
                    if (!refundableMatches(rateRefundable, refundable)) {
                        logger.info("      Refundable mismatch (wanted: {}, got: {})", 
                            refundable, rateRefundable);
                        continue;
                    }
                    
                    logger.info("      Refundable matches!");
                    logger.info("");
                    logger.info("      MATCH FOUND!");
                    
                    // Click Select Rate
                    if (hotelPage.clickSelectRate(rateIndex)) {
                        matchFound = true;
                        matchDetails = String.format(
                            "Hotel: %s, Page: %d, Rate: %d, Provider: %s, Refundable: %s",
                            hotelName, pageNum, rateIndex + 1, rateProvider, rateRefundable
                        );
                        logger.info("      Rate selected successfully!");
                    } else {
                        logger.info("      Failed to click Select Rate");
                    }
                }
                
                // Collapse rates if no match found for this hotel
                if (!matchFound) {
                    hotelPage.hideAvailability(hotelIndex);
                }
                
                logger.info("-------------------------------------------------------------");
            }
        }
        
        // Final result
        logger.info("===================================================================");
        if (matchFound) {
            logger.info("RATE SELECTION SUCCESSFUL");
            logger.info("   {}", matchDetails);
            Allure.step("Rate selected: " + matchDetails);
        } else {
            String errorMsg = String.format(
                "No rate found matching Provider: '%s' with Refundable: '%s' after searching %d pages",
                provider, refundable, totalPages
            );
            logger.error("RATE SELECTION FAILED");
            logger.error("   {}", errorMsg);
            Allure.step("Rate selection failed: " + errorMsg);
            throw new AssertionError(errorMsg);
        }
        logger.info("===================================================================");
    }
    
    /**
     * Check if provider strings match (case-insensitive, handles partial matches)
     */
    private boolean providerMatches(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualLower = actual.toLowerCase().trim();
        String expectedLower = expected.toLowerCase().trim();
        
        // Handle "None" case
        if (actualLower.equals("none") || actualLower.isEmpty()) {
            return false;
        }
        
        // Exact match or contains match
        return actualLower.equals(expectedLower) || 
               actualLower.contains(expectedLower) || 
               expectedLower.contains(actualLower);
    }
    
    /**
     * Check if refundable status matches
     * Logic: If actual contains "non refundable" or "nonrefundable" -> NOT refundable
     *        Everything else is considered REFUNDABLE
     */
    private boolean refundableMatches(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualLower = actual.toLowerCase().trim();
        String expectedLower = expected.toLowerCase().trim();
        
        // Check if actual contains "non refundable", "nonrefundable", "non-refundable", "non fundable", "nonfundable" - then it's NOT refundable
        // Everything else is considered REFUNDABLE
        boolean actualIsNonRefundable = actualLower.contains("non refundable") || 
                                        actualLower.contains("nonrefundable") ||
                                        actualLower.contains("non-refundable") ||
                                        actualLower.contains("non fundable") ||
                                        actualLower.contains("nonfundable") ||
                                        actualLower.contains("non-fundable");
        boolean actualIsRefundable = !actualIsNonRefundable;
        
        // Check what user expects
        boolean expectedIsYes = expectedLower.equals("yes") || expectedLower.equals("true") || 
                                expectedLower.equals("refundable");
        boolean expectedIsNo = expectedLower.equals("no") || expectedLower.equals("false") || 
                               expectedLower.equals("non-refundable") || expectedLower.equals("nonrefundable");
        
        return (actualIsRefundable && expectedIsYes) || (actualIsNonRefundable && expectedIsNo);
    }
    
    /**
     * Alternative step: Select first available rate from provider (ignoring refundable)
     */
    @And("Select any Rate Plan from {string}")
    @When("Select any Rate Plan from {string}")
    @Step("Select any Rate Plan from provider '{0}'")
    public void selectAnyRatePlanFromProvider(String provider) {
        logger.info("===================================================================");
        logger.info("STARTING RATE SELECTION (Any Rate from Provider)");
        logger.info("   Provider: {}", provider);
        logger.info("===================================================================");
        
        hotelPage = new HotelSearchResultsPage(page());
        
        int totalPages = hotelPage.getPageCount();
        boolean matchFound = false;
        
        for (int pageNum = 1; pageNum <= totalPages && !matchFound; pageNum++) {
            if (pageNum > 1) {
                hotelPage.goToPage(pageNum);
            }
            
            List<Locator> hotels = hotelPage.getAllHotels();
            
            for (int hotelIndex = 0; hotelIndex < hotels.size() && !matchFound; hotelIndex++) {
                String hotelProvider = hotelPage.getHotelActiveProvider(hotelIndex);
                
                if (!providerMatches(hotelProvider, provider)) {
                    continue;
                }
                
                if (!hotelPage.clickCheckAvailability(hotelIndex)) {
                    continue;
                }
                
                if (!hotelPage.hasRatesAvailable(hotelIndex)) {
                    hotelPage.hideAvailability(hotelIndex);
                    continue;
                }
                
                // Select the first available rate
                if (hotelPage.clickSelectRate(0)) {
                    matchFound = true;
                    logger.info("First available rate selected from {}", 
                        hotelPage.getHotelName(hotelIndex));
                }
            }
        }
        
        if (!matchFound) {
            throw new AssertionError("No rate found from provider: " + provider);
        }
    }
    
    /**
     * Step to click on a specific page number in pagination
     */
    @And("click on page {int} in hotel results")
    @When("click on page {int} in hotel results")
    public void clickOnPageInHotelResults(int pageNumber) {
        hotelPage = new HotelSearchResultsPage(page());
        hotelPage.goToPage(pageNumber);
        logger.info("Navigated to page {} in hotel results", pageNumber);
    }
}
