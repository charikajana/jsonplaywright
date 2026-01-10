Feature: E-Commerce Product Search

  Scenario: Search for a product
    Given "Open the application 'https://www.saucedemo.com/'"
    When "Type 'standard_user' into username"
    And "Type 'secret_sauce' into password"
    And "Click on Login button"
    Then "Verify inventory is visible"
