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

## 🏢 5. Windows & Pro Utilities

### `SWITCH_WINDOW`
Switches focus to another tab/window by index or title.
- **Gherkin**: `When User switches to window "Payment Success"`
- **JSON**: `{ "actionType": "SWITCH_WINDOW", "value": "___RUNTIME_PARAMETER___" }`

### `CLICK_AND_SWITCH`
The "Popup Pattern" - Clicks an element and waits for a new window to open.
- **Gherkin**: `When User clicks "Help" and switches to help portal`
- **JSON**: `{ "actionType": "CLICK_AND_SWITCH", "element": { "text": "Help" } }`

### `WAIT_STABLE`
Waits until all network activity has ceased (LoadState.NetworkIdle).
- **Gherkin**: `And User waits for page to stabilize`
- **JSON**: `{ "actionType": "WAIT_STABLE" }`

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
