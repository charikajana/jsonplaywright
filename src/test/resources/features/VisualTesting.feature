@visual_testing @regression
Feature: Visual Regression Testing

  Background:
    Given Open the application "https://practicetestautomation.com/practice-test-login/"

  @baseline
  Scenario: Capture visual baseline for login page
    When I capture visual baseline for "LoginPage"
    Then visual difference should be less than 1%

  @visual_regression
  Scenario: Verify login page visual regression
    Then I verify visual regression for "LoginPage"
    And visual difference should be less than 2%

  @visual_regression @strict
  Scenario: Strict visual regression with low tolerance
    Then I verify visual regression for "LoginPage" with 0.5% tolerance

  @visual_update_baseline
  Scenario: Update baseline after intentional UI change
    When I update visual baseline for "LoginPage"
    Then I verify visual regression for "LoginPage"
