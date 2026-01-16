@DynamicDataTest
Feature: Dynamic Data Verification (Random and Variables)

  Scenario: Verify Random Data Generation and Variable Capture/Reuse
    Given User navigates to "https://adactinhotelapp.com/"
    When User enters "RANDOM_FIRST_NAME" into Username field
    And User enters "RANDOM_NUMERIC_8" into Password field
    And User captures the text from Header into "VAR_TEST_TITLE"
    Then User should see "VAR_TEST_TITLE" displayed in the log
    
    # Second part to verify persistence
    When User enters "VAR_TEST_TITLE" into Username field
    And User enters "RANDOM_FIRST_NAME" into Password field
    Then User verifies Username field contains "VAR_TEST_TITLE"
