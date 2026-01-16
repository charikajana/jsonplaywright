@HotelBooker @TravelPolicy
Feature: HotelBooker Travel Policy Management
  As an agent admin user
  I want to manage travel policy settings
  So that I can control booking policies for clients

  @UpdateTravelPolicy
  Scenario Outline: Update Travel Policy for Test QA Client
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
    When user selects country "<country>"
    And enters location "<location>" from suggestion
    And selects distance "<distance>"
    And enters number of nights as "<nights>"
    And selects number of rooms as "<rooms>"
    And selects number of guests as "<guests>"
    And selects arrival date "<days>" days from today
    And clicks on search button
    And Select the Rate Plan from "<provider>" with refundable "<refundable>"


     Examples:
      | country | location   | hotelName | distance | days | nights | rooms | guests | provider    | refundable |  payment_method | cancel_method    | email_recipients    |
      | USA     | Dallas     | Holiday   | 20 Miles | 25   | 1      | 1     | 1      | Sabre       | No         |  Credit Card    | Cancel by Room   | Booker,Agent,Client |
