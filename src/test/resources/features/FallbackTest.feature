Feature: Fallback to Traditional Steps
  
  @traditional @sanity
  Scenario: Verify traditional step fallback
    Given Open custom URL "https://example.com"
    Then Verify page title contains "Example Domain"
