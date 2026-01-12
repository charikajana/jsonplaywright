@Sabre_vs_Sabre_Sanity @Sanity @TestOne
Feature: Sabre vs Sabre Single Night Single Room Single Guest Booking

  Background:
    Given Open Browser and Navigate to "https://hotelbooker.cert.sabre.com"
    When user enters username "QA_Sabre" and password "Te5t@1234"
    And user clicks login button
    And selects client "Test QA Client(Sabre)"
    Then Validate selected client should display on header
    And Click on Tools
    And Click on Agency Admin
    When user enters username "QA_Sabre" and password "Te5t@1234"
    And user clicks login button
    And Select "Test QA Client(Sabre)" from dropdown
    And click on Travel Policy
    And uncheck Policy Active Check box
    And click on Update Policy

  Scenario Outline: Sabre vs BCOM Single Night Single Room Single Guest Standard Booking
    # SHOP Phase
    When user selects country "<country>"
    And enters location "<location>" from suggestion
    And selects distance "<distance>"
    And enters number of nights as "<nights>"
    And selects number of rooms as "<rooms>"
    And selects arrival date "<days>" days from today
    And clicks on search button
    Then hotel search results should be displayed or a message if no hotels found
    And Select the Rate Plan from "<provider>" with refundable "<refundable>"
    # BOOK Phase
    And Click on Full Rate Information
    And Add Booking Contact details
    And Add Traveller details
    And Click on Fax Communication Preference and Add Custom Data Fields
    And Select Payment Method as "<payment_method>"
    And Click on Finish button
    Then Validate Booking Reference Number to be display
    And Click on Home button
    #RETRIVE Phase
    And user clicks on View Bookings tab
    And user enters booking reference
    And user clicks search button for booking retrieval
    Then Validate booking retrieval page should be displayed
    # CANCEL Phase
    When clicks Cancel Booking and confirms cancellation
    Then booking status should be displayed as Cancelled
    #Logout Phase
    And user clicks user menu
    And user clicks logout button

    Examples:
      | country | location   | hotelName | distance | days | nights | rooms | guests | provider    | refundable |  payment_method | cancel_method    | email_recipients    |
      | USA     | Dalla     | Holiday   | 20 Miles | 25   | 1      | 2     | 1      | Sabre       | No         |  Credit Card    | Cancel by Room   | Booker,Agent,Client |