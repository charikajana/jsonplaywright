@accessibility @a11y @wcag
Feature: Accessibility Testing

  Background:
    Given Open the application "https://practicetestautomation.com/practice-test-login/"

  @accessibility_audit
  Scenario: Run accessibility audit on login page
    Then I run accessibility audit for "LoginPage"
    And I log accessibility report
    And accessibility audit should pass

  @accessibility_critical
  Scenario: Verify critical accessibility violations
    Then I run accessibility audit
    And accessibility critical violations should be 0 or less

  @accessibility_tolerant
  Scenario: Allow some non-critical violations
    Then I run accessibility audit for "LoginPage"
    And accessibility critical violations should be 0 or less
    And total accessibility violations should be 5 or less

  @accessibility_full_audit
  Scenario: Complete accessibility audit with detailed checks
    When Type "student" into username
    And Type "Password123" into password
    And Click on Submit button
    Then Verify "Logged In Successfully" is visible
    And I run accessibility audit for "LoggedInPage"
    And accessibility audit should pass
