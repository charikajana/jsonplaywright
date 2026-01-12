@RecordedTest
Feature: Sample Login Flow
  
  Scenario: Verify successful login using recorded steps
    Given Open Browser and Navigate to "https://practicetestautomation.com/practice-test-login/"
    When I click on username
    And I enter text into username
    And I click on password
    And I enter text into password
    And I click on Submit
    Then I should see the text "Logged In Successfully"
