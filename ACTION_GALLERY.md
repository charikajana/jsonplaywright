# 🗂️ JSON Framework Action Gallery

This is a complete reference of every action supported by the framework when using the **JSON Repository** (No-Code approach).

---

## 🌐 1. Navigation & Screenshots

### `NAVIGATE`
Navigates the browser to a specific URL.
- **Gherkin**: `Given User navigates to "https://google.com"`
- **JSON**: `{ "actionType": "NAVIGATE", "value": "___RUNTIME_PARAMETER___" }`

### `SCREENSHOT`
Captures a full-page screenshot and attaches it to Allure.
- **Gherkin**: `Then User takes a screenshot of the login page`
- **JSON**: `{ "actionType": "SCREENSHOT", "description": "Login_Page" }`

---

## 🖱️ 2. Mouse & Keyboard Interactions

### `CLICK` / `DOUBLE_CLICK` / `RIGHT_CLICK`
Basic click interactions on an element.
- **Gherkin**: `When User clicks the "Submit" button`
- **JSON**: `{ "actionType": "CLICK", "element": { "text": "Submit" } }`

### `HOVER`
Moves the mouse over an element.
- **Gherkin**: `When User hovers over the profile icon`
- **JSON**: `{ "actionType": "HOVER", "element": { "id": "profile" } }`

### `DRAG_AND_DROP`
Drags a source element to a target element.
- **Gherkin**: `When User drags the item to the cart`
- **JSON**: `{ "actionType": "DRAG_AND_DROP", "element": { "id": "item" }, "targetElement": { "id": "cart" } }`

### `SCROLL_TO`
Scrolls the specified element into the view.
- **Gherkin**: `When User scrolls to the "Contact Us" section`
- **JSON**: `{ "actionType": "SCROLL_TO", "element": { "css": "footer" } }`

---

## ✍️ 3. Form & Input Interactions

### `TYPE` / `CLEAR`
Types text into or clears an input field.
- **Gherkin**: `When User enters username "admin"`
- **JSON**: `{ "actionType": "TYPE", "element": { "id": "user" }, "value": "___RUNTIME_PARAMETER___" }`

### `SELECT`
Selects an option from a dropdown (supports text, value, and date keywords).
- **Gherkin**: `When User selects "Economy" from class dropdown`
- **JSON**: `{ "actionType": "SELECT", "element": { "id": "class" }, "value": "___RUNTIME_PARAMETER___" }`

### `CHECK` / `UNCHECK`
Checks or unchecks checkboxes and radio buttons.
- **Gherkin**: `When User checks the terms and conditions`
- **JSON**: `{ "actionType": "CHECK", "element": { "id": "terms" } }`

### `PRESS_KEY`
Simulates pressing a single key (Enter, Escape, Tab).
- **Gherkin**: `When User presses "Enter" key`
- **JSON**: `{ "actionType": "PRESS_KEY", "element": { "id": "search" }, "value": "Enter" }`

### `UPLOAD_FILE`
Uploads a file to a specific input element.
- **Gherkin**: `When User uploads resume "C:\temp\cv.pdf"`
- **JSON**: `{ "actionType": "UPLOAD_FILE", "element": { "id": "fileInput" }, "value": "___RUNTIME_PARAMETER___" }`

---

## 🛑 4. Verifications (Assertions)

### `VERIFY_TEXT`
Asserts that a **specific element** (defined in the JSON locator) contains the expected text. There are no predefined locators like `h1`; it uses exactly what you provide.
- **Gherkin**: `Then User should see "Welcome Back"`
- **JSON**: 
```json
{ 
  "actionType": "VERIFY_TEXT", 
  "description": "Welcome Header",
  "element": { "css": ".dashboard-title", "id": "welcome-msg" }, 
  "value": "___RUNTIME_PARAMETER___" 
}
```

### `VERIFY_ELEMENT` / `VERIFY_NOT_VISIBLE`
Asserts visibility or absence of an element.
- **Gherkin**: `Then User should see error message`
- **JSON**: `{ "actionType": "VERIFY_ELEMENT", "element": { "id": "error" } }`

### `VERIFY_ATTRIBUTE`
Asserts an attribute value (e.g., href, src, title).
- **Gherkin**: `Then User verifies link points to "privacy.html"`
- **JSON**: `{ "actionType": "VERIFY_ATTRIBUTE", "element": { "id": "link" }, "value": "href:privacy.html" }`

### `VERIFY_CSS`
Asserts a style property (e.g., color, font-size).
- **Gherkin**: `Then User verifies button is red`
- **JSON**: `{ "actionType": "VERIFY_CSS", "element": { "id": "btn" }, "value": "background-color:rgb(255, 0, 0)" }`

---

## 🏢 5. Windows, Waits & Pro Utilities

### `SWITCH_WINDOW`
Switches focus to another tab/window by index, title, or keyword.
- **Values**: `"new"` (last window), `"main"` or `"0"` (first window), `"1"`, `"2"` (by index), or partial URL/title match
- **Gherkin**: `When User switches to window "Payment Success"`
- **JSON**: `{ "actionType": "SWITCH_WINDOW", "value": "new" }`

### `CLICK_AND_SWITCH`
**CRITICAL for Popup Windows** - Clicks an element that opens a new window/tab, waits for it to open, and automatically switches to it. Use this instead of `CLICK` when:
- Clicking a link with `target="_blank"`
- Clicking a button that opens a popup
- Opening admin panels, help portals, or external links
- **Gherkin**: `When User clicks "Agency Admin" and switches to new window`
- **JSON**:
```json
{
  "actionType": "CLICK_AND_SWITCH",
  "description": "Click link and switch to popup window",
  "element": { "text": "Agency Admin", "selector": "a:has-text('Agency Admin')" }
}
```
- **Follow with**: `WAIT_STABLE` to ensure the new window is fully loaded.

### `CLOSE_WINDOW`
Closes the current window and switches back to the main (first) window. Essential for cleanup after popup operations.
- **Gherkin**: `And User closes the current window and switches to main window`
- **JSON**:
```json
{
  "actionType": "CLOSE_WINDOW",
  "description": "Close popup and return to main window"
}
```

### `WAIT_STABLE`
Waits until all network activity has ceased (DOM loaded, Network idle, JavaScript stable).
- **Use after**: Page navigation, form submission, AJAX calls
- **Gherkin**: `And User waits for page to stabilize`
- **JSON**: `{ "actionType": "WAIT_STABLE", "description": "Wait for page to stabilize after login" }`

### `WAIT_FOR_RELOAD`
**CRITICAL for Page Refresh Scenarios** - Waits for a page to completely reload after an action that triggers a refresh (NOT a navigation). Use when:
- Selecting from a dropdown that causes page postback
- AJAX operations that reload the page content
- Form auto-submits that refresh the current page
- **Gherkin**: `And User waits for page to reload after selection`
- **JSON**:
```json
{
  "actionType": "WAIT_FOR_RELOAD",
  "description": "Wait for page to reload after client selection",
  "timeout": 30000
}
```
- **Difference from WAIT_STABLE**: `WAIT_FOR_RELOAD` adds a 500ms delay before checking, ensuring the reload has started. `WAIT_STABLE` checks immediately.

### `HANDLE_DIALOG`
Sets up a listener to handle the next JavaScript Alert/Confirm.
- **Gherkin**: `When User accepts the upcoming alert`
- **JSON**: `{ "actionType": "HANDLE_DIALOG", "value": "accept" }`

### `JS_EVALUATE`
Injects custom JavaScript into the browser page.
- **Gherkin**: `When User clears local storage`
- **JSON**: `{ "actionType": "JS_EVALUATE", "value": "localStorage.clear();" }`

---

## 📥 6. Capture & Dynamic Data

### `GET_TEXT` (Variable Capture)
Extracts text from an element and stores it in a scenario-scoped variable.
- **Gherkin**: `Then User captures the booking ID`
- **JSON**: 
```json
{ 
  "actionType": "GET_TEXT", 
  "description": "Store Booking Number",
  "element": { "css": ".booking-ref" }, 
  "value": "VAR_BOOKING_ID" 
}
```

### 🎲 Dynamic Keywords (RANDOM & VAR)
Any `TYPE`, `SELECT`, `NAVIGATE`, or `VERIFY_TEXT` action can use these keywords in the `value` field.

| Keyword Type | Usage | Description |
| :--- | :--- | :--- |
| **Random Data** | `RANDOM_EMAIL`, `RANDOM_FIRST_NAME` | Generates new test data. |
| **Variable Reuse**| `VAR_BOOKING_ID` | Refers to a value previously captured. |
| **Numeric/Alpha** | `RANDOM_NUMERIC_5` | Generates data of specific length. |

**Example Usage in JSON:**
```json
{
  "actionType": "VERIFY_TEXT",
  "description": "Confirm username box contains the dynamic title",
  "expectedText": "VAR_TEST_TITLE",
  "comparisonType": "CONTAINS",
  "element": { "id": "username" }
}
```

*Note: `comparisonType` defaults to `CONTAINS` if not specified. Supported values: `EXACTLY`, `CONTAINS`.*

---

## 🚨 7. Common Patterns & Troubleshooting

### Pattern 1: Popup Window Workflow
When clicking a link/button that opens a new window:
```json
{
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "CLICK_AND_SWITCH",
      "description": "Click Admin link and switch to popup",
      "element": { "selector": "a:has-text('Admin')" }
    },
    {
      "actionNumber": 2,
      "actionType": "WAIT_STABLE",
      "description": "Wait for popup window to fully load"
    }
  ]
}
```
**At the end of the scenario**, close the popup:
```json
{
  "actionType": "CLOSE_WINDOW",
  "description": "Close popup and return to main window"
}
```

### Pattern 2: Dropdown Selection with Page Refresh
When selecting from a dropdown that triggers a page reload/postback:
```json
{
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "SELECT",
      "description": "Select client from dropdown",
      "element": { "id": "clientDropdown" },
      "value": "___RUNTIME_PARAMETER___"
    },
    {
      "actionNumber": 2,
      "actionType": "WAIT_FOR_RELOAD",
      "description": "Wait for page to reload after client selection",
      "timeout": 30000
    }
  ]
}
```

### Pattern 3: Click with Navigation
When clicking a link that navigates to a new page (same window):
```json
{
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "CLICK",
      "description": "Click Travel Policy link",
      "element": { "selector": "a[href*='TravelPolicy']" }
    },
    {
      "actionNumber": 2,
      "actionType": "WAIT_STABLE",
      "description": "Wait for new page to load"
    }
  ]
}
```

---

## ⚠️ 8. Best Practices for Robust Locators

### DO: Use Simple, Stable Selectors
```json
"selector": "a[href*='TravelPolicy']"        ✅ Simple, stable
"selector": "#loginButton"                    ✅ ID-based
"selector": "button:has-text('Submit')"       ✅ Text-based
```

### DON'T: Use Complex, Fragile Selectors
```json
"selector": "div.container > div:nth-child(3) > a"  ❌ Position-dependent
"selector": "a[href='/TravelPolicyUpdate.aspx']:has(h2:text('Travel Policy')):near(:text('Employee'))"  ❌ Too complex
```

### Selector Priority (Best to Worst):
1. **ID**: `#elementId` - Most stable
2. **Name**: `[name='fieldName']` - Very stable
3. **Data attributes**: `[data-test='login-btn']` - Designed for testing
4. **ARIA**: `[aria-label='Submit']` - Accessible and stable
5. **CSS with text**: `button:has-text('Login')` - Readable
6. **Href/Src contains**: `a[href*='keyword']` - URL-based
7. **XPath**: Only for complex DOM traversal

### When Element Not Found:
1. Verify the page has fully loaded (add `WAIT_STABLE` or `WAIT_FOR_RELOAD`)
2. Check if the element is in a popup/iframe
3. Simplify the selector - remove `:near()` and `:has()` pseudo-selectors
4. Use browser DevTools to verify the element exists

---

## 📋 Complete Action Type Reference

| Action Type | Handler | Description |
|-------------|---------|-------------|
| `NAVIGATE` | NavigationHandler | Navigate to URL |
| `CLICK` | InteractionHandler | Single click |
| `DOUBLE_CLICK` | InteractionHandler | Double click |
| `RIGHT_CLICK` | InteractionHandler | Context menu click |
| `TYPE` | InteractionHandler | Enter text |
| `CLEAR` | InteractionHandler | Clear input field |
| `SELECT` | InteractionHandler | Select dropdown option |
| `SELECT_DROPDOWN` | InteractionHandler | Alias for SELECT |
| `SELECT_DATE` | InteractionHandler | Date picker selection |
| `HOVER` | InteractionHandler | Mouse hover |
| `CHECK` | InteractionHandler | Check checkbox |
| `UNCHECK` | InteractionHandler | Uncheck checkbox |
| `PRESS_KEY` | InteractionHandler | Keyboard key press |
| `SWITCH_WINDOW` | NavigationHandler | Switch to window |
| `CLICK_AND_SWITCH` | InteractionHandler | Click + switch to popup |
| `CLOSE_WINDOW` | NavigationHandler | Close current window |
| `DRAG_DROP` | InteractionHandler | Drag and drop |
| `GET_TEXT` | InteractionHandler | Capture text to variable |
| `SCROLL` | NavigationHandler | Scroll to element |
| `WAIT_NAVIGATION` | NavigationHandler | Wait for navigation |
| `WAIT_STABLE` | NavigationHandler | Wait for page stability |
| `WAIT_FOR_RELOAD` | NavigationHandler | Wait for page reload |
| `VERIFY_TEXT` | VerificationHandler | Assert text content |
| `VERIFY_ELEMENT` | VerificationHandler | Assert element visible |
| `VERIFY_ELEMENTS` | VerificationHandler | Assert multiple elements |
| `SCREENSHOT` | NavigationHandler | Capture screenshot |
