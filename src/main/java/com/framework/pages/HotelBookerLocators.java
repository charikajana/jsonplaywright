package com.framework.pages;

/**
 * Page Object Model - Locators for HotelBooker Application
 * Centralized locator definitions for maintainability.
 */
public class HotelBookerLocators {

    // ==================== Login Page ====================
    public static final String USERNAME_FIELD = "#ctl00_cphMainContent_txtUserName";
    public static final String PASSWORD_FIELD = "#ctl00_cphMainContent_txtPassword";
    public static final String LOGIN_BUTTON = "#ctl00_cphMainContent_btnLogin";
    
    // ==================== Agency Admin Login ====================
    public static final String ADMIN_USERNAME_FIELD = "#txtUserName";
    public static final String ADMIN_PASSWORD_FIELD = "#txtPassword";
    public static final String ADMIN_LOGIN_BUTTON = "#btnLogin";
    
    // ==================== Hotel Search Results Page ====================
    
    // Pagination
    public static final String PAGINATION_CONTAINER = "//strong[text()='Pages:']//parent::*";
    public static final String PAGINATION_LINKS = "//strong[text()='Pages:']/following-sibling::a";
    public static final String PAGE_LINK_TEMPLATE = "//strong[text()='Pages:']/following-sibling::a[text()='%d']";
    public static final String NEXT_PAGE_LINK = "a:has-text('Next')";
    public static final String PREVIOUS_PAGE_LINK = "a:has-text('Previous')";
    
    // Hotel Cards
    public static final String HOTEL_NAME_LINKS = "a[id*='hotelNameLink']";
    public static final String ACTIVE_CONTENT_PROVIDER = "//div[contains(text(),'Active Content Providers')]/following-sibling::ul/li";
    public static final String ACTIVE_PROVIDER_BY_INDEX = "(//div[contains(text(),'Active Content Providers')])[%d]/following-sibling::ul/li";
    
    // Availability Actions
    public static final String CHECK_AVAILABILITY = "a:has-text('Check Availability')";
    public static final String HIDE_AVAILABILITY = "a:has-text('Hide Availability')";
    public static final String RATES_LOADING_INDICATOR = "text=Checking Availability";
    public static final String NO_AVAILABILITY = "text=No Online Availability";
    
    // Rate Selection
    public static final String RATE_ROW = "//div[contains(.,'Online Rate') and .//a[contains(text(),'Select Rate')]]";
    public static final String RATE_PROVIDER_TEXT = "(Sabre)";  // Pattern: (Provider)
    public static final String FULL_RATE_INFO = "a:has-text('Full Rate Information')";
    public static final String LESS_INFO = "a:has-text('Less Information')";
    public static final String SELECT_RATE = "a:has-text('Select Rate')";
    
    // Rate Details
    public static final String CANCELLATION_POLICY = "//div[contains(text(),'Cancellation Policy')]/following-sibling::p";
    public static final String DEPOSIT_POLICY = "//div[contains(text(),'Deposit Policy')]/following-sibling::p";
    public static final String RATE_BREAKDOWN = "//div[contains(text(),'Rate Breakdown')]";
    
}
