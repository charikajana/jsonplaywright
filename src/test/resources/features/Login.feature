@login @sanity
Feature: User Login Flow

  @issue=Bug_001 @tmsLink=TCM_20 @rally=RallyID_20
  Scenario Outline: Valid Login
    Given Open the application "https://practicetestautomation.com/practice-test-login/"
    When Type "<username>" into username
    And Type "<password>" into password  
    And Click on Submit button
    Then Verify "Logged In Successfully" is visible

    Examples:
    | username | password      |
    | student  | Password123   |
    | student    | Password123 |
    | student    | Password123 |
