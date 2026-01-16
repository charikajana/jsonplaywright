@HotelBooker @TravelPolicy
Feature: HotelBooker Travel Policy Management
  As an agent admin user
  I want to manage travel policy settings
  So that I can control booking policies for clients

  @UpdateTravelPolicy
  Scenario: Update Travel Policy for Test QA Client
    Given Open Browser and Navigate to HotelBooker
    When user enters username and password
    And user clicks login button
    And selects client "Test QA Client(Sabre)"
    Then Validate selected client should display on header
    And click on Tools
    And click on Agency Admin and switch to new window
    And Enter username and password click on Login button
    And select "Test QA Client(Sabre)" from drop down
    And click on Travel Policy
    And uncheck the Policy Active Check box
    And click on update policy button
    Then Verify "Update Successful!" message is displayed
    And Close the current window and switch to main window
