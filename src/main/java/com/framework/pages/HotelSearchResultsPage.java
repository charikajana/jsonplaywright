package com.framework.pages;

import com.framework.playwright.PlaywrightManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object for Hotel Search Results Page (Hotels.aspx)
 * Handles hotel listing, rate selection, and pagination operations.
 */
public class HotelSearchResultsPage {
    private static final Logger logger = LoggerFactory.getLogger(HotelSearchResultsPage.class);
    
    private final Page page;
    
    // ==================== Locators ====================
    
    // Pagination
    private static final String PAGINATION_CONTAINER = "//strong[text()='Pages:']//parent::*";
    private static final String PAGINATION_LINKS = "//strong[text()='Pages:']/following-sibling::a";
    private static final String PAGE_LINK_BY_NUMBER = "//strong[text()='Pages:']/following-sibling::a[text()='%d']";
    
    // Hotel Cards - Using the repeater pattern from the UI
    private static final String HOTEL_CARDS = "[id*='HotelRepeater']";
    private static final String ALL_HOTEL_CONTAINERS = "//a[contains(text(),'Check Availability') or contains(text(),'Hide Availability')]//ancestor::div[contains(@class,'hotel') or position()=3]";
    
    // Active Content Provider at Hotel Level
    private static final String HOTEL_ACTIVE_PROVIDER = ".//div[contains(text(),'Active Content Providers')]/following-sibling::ul/li";
    private static final String HOTEL_ACTIVE_PROVIDER_TEXT = "Active Content Providers";
    
    // Check Availability Button
    private static final String CHECK_AVAILABILITY_LINK = "a:has-text('Check Availability')";
    private static final String HIDE_AVAILABILITY_LINK = "a:has-text('Hide Availability')";
    
    // Rate Rows (after expanding availability)
    private static final String RATE_ROWS = "//div[contains(@class,'rate-row') or (contains(.,'Online Rate') and contains(.,'Select Rate'))]";
    private static final String RATE_PROVIDER_PATTERN = "\\(([^)]+)\\)"; // Matches (Sabre), (Amadeus), etc.
    
    // Full Rate Information
    private static final String FULL_RATE_INFO_LINK = "a:has-text('Full Rate Information')";
    private static final String LESS_INFO_LINK = "a:has-text('Less Information')";
    
    // Rate Details
    private static final String CANCELLATION_POLICY = "//div[contains(text(),'Cancellation Policy')]/following-sibling::p | //text()[contains(.,'Cancellation Policy')]/following::p[1]";
    private static final String SELECT_RATE_BUTTON = "a:has-text('Select Rate')";
    
    // Loading indicators
    private static final String RATES_LOADING = "text=Checking Availability";
    private static final String RETRIEVING_RATES = "text=Retrieving Rates";
    private static final String NO_AVAILABILITY = "text=No Online Availability";
    
    public HotelSearchResultsPage() {
        this.page = PlaywrightManager.getInstance().getPage();
    }
    
    public HotelSearchResultsPage(Page page) {
        this.page = page;
    }
    
    // ==================== Pagination Methods ====================
    
    /**
     * Get the total number of pages available
     */
    public int getPageCount() {
        try {
            Locator pageLinks = page.locator(PAGINATION_LINKS);
            int count = pageLinks.count();
            logger.info("[PAGINATION] Found {} page links", count);
            return count > 0 ? count : 1;
        } catch (Exception e) {
            logger.warn("[PAGINATION] Could not determine page count, defaulting to 1");
            return 1;
        }
    }
    
    /**
     * Navigate to a specific page number
     */
    public void goToPage(int pageNumber) {
        logger.info("[PAGINATION] Navigating to page {}", pageNumber);
        String pageXpath = String.format(PAGE_LINK_BY_NUMBER, pageNumber);
        Locator pageLink = page.locator(pageXpath);
        
        if (pageLink.count() > 0) {
            pageLink.first().click();
            waitForPageLoad();
            waitForHotelsToLoad();
            logger.info("[PAGINATION] Successfully navigated to page {}", pageNumber);
        } else {
            logger.warn("[PAGINATION] Page {} link not found", pageNumber);
        }
    }
    
    /**
     * Check if currently on a specific page
     */
    public boolean isOnPage(int pageNumber) {
        // Current page typically shown differently (bold, not a link, etc.)
        String url = page.url();
        return url.contains("page=" + pageNumber) || (pageNumber == 1 && !url.contains("page="));
    }
    
    // ==================== Hotel Methods ====================
    
    /**
     * Get all hotel elements on the current page
     */
    public List<Locator> getAllHotels() {
        // Find all Check Availability / Hide Availability links and get their parent containers
        Locator checkAvailLinks = page.locator("a:has-text('Check Availability'), a:has-text('Hide Availability')");
        int hotelCount = checkAvailLinks.count();
        logger.info("[HOTELS] Found {} hotels on current page", hotelCount);
        return List.of(checkAvailLinks.all().toArray(new Locator[0]));
    }
    
    /**
     * Get the Active Content Provider for a hotel
     * @param hotelIndex 0-based index of the hotel on the page
     */
    public String getHotelActiveProvider(int hotelIndex) {
        try {
            // Navigate to the hotel's provider section
            String providerXpath = String.format(
                "(//div[contains(text(),'Active Content Providers')])[%d]/following-sibling::ul/li", 
                hotelIndex + 1
            );
            Locator providerElement = page.locator(providerXpath);
            
            if (providerElement.count() > 0) {
                String provider = providerElement.first().textContent().trim();
                logger.info("[HOTEL {}] Active Content Provider: {}", hotelIndex, provider);
                return provider;
            }
            return "None";
        } catch (Exception e) {
            logger.warn("[HOTEL {}] Could not get provider: {}", hotelIndex, e.getMessage());
            return "None";
        }
    }
    
    /**
     * Click Check Availability for a hotel by index
     */
    public boolean clickCheckAvailability(int hotelIndex) {
        try {
            Locator checkAvailLinks = page.locator(CHECK_AVAILABILITY_LINK);
            
            if (checkAvailLinks.count() > hotelIndex) {
                logger.info("[HOTEL {}] Clicking Check Availability", hotelIndex);
                checkAvailLinks.nth(hotelIndex).click();
                waitForRatesToLoad(hotelIndex);
                return true;
            }
            logger.warn("[HOTEL {}] Check Availability button not found", hotelIndex);
            return false;
        } catch (Exception e) {
            logger.error("[HOTEL {}] Failed to click Check Availability: {}", hotelIndex, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if hotel has rates loaded (not "No Online Availability")
     */
    public boolean hasRatesAvailable(int hotelIndex) {
        try {
            // Check for "No Online Availability" text near this hotel
            String noAvailXpath = String.format(
                "(//a[contains(text(),'Hide Availability')])[%d]//ancestor::div[1]//*[contains(text(),'No Online Availability') or contains(text(),'no availability')]",
                hotelIndex + 1
            );
            Locator noAvailText = page.locator(noAvailXpath);
            
            if (noAvailText.count() > 0) {
                logger.info("[HOTEL {}] No online availability", hotelIndex);
                return false;
            }
            return true;
        } catch (Exception e) {
            return true; // Assume rates available if check fails
        }
    }
    
    // ==================== Rate Methods ====================
    
    /**
     * Get all rate rows for the currently expanded hotel
     */
    public Locator getRateRows(int hotelIndex) {
        // Find rate rows that contain "Online Rate" and "Select Rate"
        String rateRowsSelector = String.format(
            "xpath=(//a[contains(text(),'Hide Availability')])[%d]/ancestor::div[1]/following-sibling::div//a[contains(text(),'Select Rate')]/ancestor::div[contains(@class,'row') or position()=3]",
            hotelIndex + 1
        );
        
        // Fallback to simpler selector
        Locator rateRows = page.locator("a:has-text('Select Rate')");
        logger.info("[HOTEL {}] Found {} rate rows", hotelIndex, rateRows.count());
        return rateRows;
    }
    
    /**
     * Get the provider from a rate row (e.g., extracts "Sabre" from "(Sabre)")
     */
    public String getRateProvider(Locator rateRow) {
        try {
            String rateText = rateRow.locator("..").locator("..").textContent();
            Pattern pattern = Pattern.compile(RATE_PROVIDER_PATTERN);
            Matcher matcher = pattern.matcher(rateText);
            
            if (matcher.find()) {
                String provider = matcher.group(1);
                logger.debug("[RATE] Provider found: {}", provider);
                return provider;
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Click Full Rate Information for a specific rate
     */
    public boolean clickFullRateInfo(int rateIndex) {
        try {
            Locator fullRateLinks = page.locator(FULL_RATE_INFO_LINK);
            
            if (fullRateLinks.count() > rateIndex) {
                logger.info("[RATE {}] Clicking Full Rate Information", rateIndex);
                fullRateLinks.nth(rateIndex).click();
                page.waitForTimeout(1000); // Wait for expansion
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("[RATE {}] Failed to click Full Rate Info: {}", rateIndex, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the cancellation policy text for an expanded rate
     */
    public String getCancellationPolicy(int rateIndex) {
        try {
            // Look for Cancellation Policy text
            Locator cancellationText = page.locator("text=Cancellation Policy").nth(rateIndex);
            if (cancellationText.count() > 0) {
                // Get the following paragraph
                Locator policyParagraph = cancellationText.locator("xpath=following::p[1]");
                if (policyParagraph.count() > 0) {
                    String policy = policyParagraph.textContent().trim();
                    logger.info("[RATE {}] Cancellation Policy: {}", rateIndex, policy);
                    return policy;
                }
            }
            return "";
        } catch (Exception e) {
            logger.warn("[RATE {}] Could not get cancellation policy: {}", rateIndex, e.getMessage());
            return "";
        }
    }
    
    /**
     * Determine if rate is refundable based on cancellation policy
     * @param cancellationPolicy The cancellation policy text
     * @return "Yes" if refundable, "No" if non-refundable
     */
    public String isRefundable(String cancellationPolicy) {
        if (cancellationPolicy == null || cancellationPolicy.isEmpty()) {
            return "Unknown";
        }
        
        String policyLower = cancellationPolicy.toLowerCase();
        
        // Non-refundable indicators
        if (policyLower.contains("non-refundable") || 
            policyLower.contains("nonrefundable") ||
            policyLower.contains("no refund") ||
            policyLower.contains("penalty applies") ||
            policyLower.contains("100 percent") ||
            policyLower.contains("no cancellation")) {
            return "No";
        }
        
        // Refundable indicators
        if (policyLower.contains("free cancellation") ||
            policyLower.contains("cancel up to") ||
            policyLower.contains("up to") ||
            policyLower.contains("days after booking") ||
            policyLower.contains("days before") ||
            policyLower.contains("refundable")) {
            return "Yes";
        }
        
        // Default to Yes if policy allows any cancellation window
        return "Yes";
    }
    
    /**
     * Click Select Rate button for a specific rate
     */
    public boolean clickSelectRate(int rateIndex) {
        try {
            Locator selectRateButtons = page.locator(SELECT_RATE_BUTTON);
            
            if (selectRateButtons.count() > rateIndex) {
                logger.info("[RATE {}] Clicking Select Rate", rateIndex);
                selectRateButtons.nth(rateIndex).click();
                waitForPageLoad();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("[RATE {}] Failed to click Select Rate: {}", rateIndex, e.getMessage());
            return false;
        }
    }
    
    // ==================== Wait Methods ====================
    
    /**
     * Wait for page to fully load
     */
    public void waitForPageLoad() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
    
    /**
     * Wait for hotels to load after page navigation
     */
    public void waitForHotelsToLoad() {
        try {
            page.waitForSelector("a:has-text('Check Availability')", 
                new Page.WaitForSelectorOptions().setTimeout(30000));
        } catch (Exception e) {
            logger.warn("[WAIT] Timeout waiting for hotels to load");
        }
    }
    
    /**
     * Wait for rates to load after clicking Check Availability
     */
    public void waitForRatesToLoad(int hotelIndex) {
        try {
            // Wait for loading indicator to disappear
            page.waitForSelector(RATES_LOADING, 
                new Page.WaitForSelectorOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                    .setTimeout(30000));
            
            // Additional wait for JavaScript
            page.waitForTimeout(2000);
            
            // Wait for either rates or "No Availability" message
            page.waitForFunction(
                "() => document.querySelector('a:has-text(\"Select Rate\")') !== null || " +
                "document.body.innerText.includes('No Online Availability') || " +
                "document.body.innerText.includes('no availability')"
            );
            
            logger.info("[HOTEL {}] Rates loaded", hotelIndex);
        } catch (Exception e) {
            logger.warn("[HOTEL {}] Timeout waiting for rates: {}", hotelIndex, e.getMessage());
        }
    }
    
    /**
     * Wait for JavaScript/AJAX operations to complete
     */
    public void waitForJavaScript() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(1000);
        } catch (Exception e) {
            logger.warn("[WAIT] JavaScript wait interrupted");
        }
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Hide availability for a hotel (collapse rates)
     */
    public void hideAvailability(int hotelIndex) {
        try {
            Locator hideLinks = page.locator(HIDE_AVAILABILITY_LINK);
            if (hideLinks.count() > hotelIndex) {
                hideLinks.nth(hotelIndex).click();
                page.waitForTimeout(500);
            }
        } catch (Exception e) {
            // Ignore - not critical
        }
    }
    
    /**
     * Get hotel name by index
     */
    public String getHotelName(int hotelIndex) {
        try {
            String hotelNameXpath = String.format(
                "(//a[contains(@href,'javascript:__doPostBack') and contains(@id,'hotelNameLink')])[%d]",
                hotelIndex + 1
            );
            Locator hotelName = page.locator(hotelNameXpath);
            if (hotelName.count() > 0) {
                return hotelName.textContent().trim();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Hotel " + (hotelIndex + 1);
    }
}
