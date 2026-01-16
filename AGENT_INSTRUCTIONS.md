# AI Agent Protocol: JSON Repository Automation

## 1. Objective
Build and maintain a **"Strong" JSON-based Locator Repository** that serves as the single source of truth for the automation framework. This repository must contain comprehensive, verified element metadata captured from a **live browser session** to enable high-performance execution and robust AI self-healing.

## 2. Mandatory Requirements

### A. Pre-Execution Check
- **Normalize Step**: Convert the Gherkin step into the exact filename format used by Java's `StepRepository`:
    1. Remove "Given/When/Then/And/But" from the start.
    2. Replace all scenario parameters (`"<param>"` or `<param>`) with `_param_`.
    3. Convert to **lowercase**.
    4. Replace all spaces and special characters with `_`.
    5. **CRITICAL**: Remove all leading/trailing underscores and duplicate underscores.
        - **End Parameter**: `selects distance "<distance>"` ➔ `selects_distance_param`
        - **Middle Parameter**: `selects arrival date <days> days from today` ➔ `selects_arrival_date_param_days_from_today`
- **Verify Existence**: Always check if `src/test/resources/locatorRepository/[normalized_name].json` already exists.

### B. Live Interaction Workflow
- **Browser Execution**: You MUST use your browser tools (Playwright/MCP) to navigate and **complete the actual step** on the live website.
- **Success Confirmation**: Ensure the action (Click, Type, etc.) was successful and that the element is in the correct state before capturing data.

### C. The "Strong" Identity Capture (17 Attributes)
For every element involved in an action, you MUST extract the following 17 attributes via JavaScript evaluation:
1. `type` (Tag name) | 2. `id` | 3. `name` | 4. `selector` | 5. `cssSelector` | 6. `xpath` (**Relative XPath** anchored to nearest ID) | 7. `text` | 8. `placeholder` | 9. `dataTest` | 10. `ariaLabel` | 11. `role` | 12. `title` | 13. `alt` | 14. `className` | 15. `value` | 16. `href` | 17. `src`.
- **DNA Fingerprinting**: Capture the element's `context` (Nearby Text, Parent Tag, Heading) and `attributes` (Role, Type) as a separate `fingerprint` object.

### D. Multi-Action Support
- **Sequence Integrity**: If a Gherkin step contains multiple intents (e.g., "Enter username and password"), you must execute each part and record them as sequential objects in the `actions` array.

### E. Execution Standards
- **Specific ActionTypes**: Map every action to these exact types: `NAVIGATE`, `CLICK`, `DOUBLE_CLICK`, `RIGHT_CLICK`, `TYPE`, `CLEAR`, `SELECT`, `SELECT_DROPDOWN`, `SELECT_DATE`, `HOVER`, `CHECK`, `UNCHECK`, `PRESS_KEY`, `SWITCH_WINDOW`, `CLICK_AND_SWITCH`, `CLOSE_WINDOW`, `DRAG_DROP`, `SCROLL`, `WAIT_STABLE`, `WAIT_FOR_RELOAD`, `WAIT_NAVIGATION`, `JS_EVALUATE`, `UPLOAD_FILE`, `HANDLE_DIALOG`, `VERIFY_TEXT`, `VERIFY_ELEMENT`, `VERIFY_ELEMENTS`, `GET_TEXT`, `SCREENSHOT`.

### F. Critical Action Selection Rules

#### When to Use `CLICK` vs `CLICK_AND_SWITCH`:
| Scenario | Action Type |
|----------|-------------|
| Link navigates in same window | `CLICK` + `WAIT_STABLE` |
| Link opens new tab/window (`target="_blank"`) | `CLICK_AND_SWITCH` + `WAIT_STABLE` |
| Button opens popup window | `CLICK_AND_SWITCH` + `WAIT_STABLE` |
| Clicking "Admin", "Tools", "Help" that opens popup | `CLICK_AND_SWITCH` + `WAIT_STABLE` |

#### When to Use `WAIT_STABLE` vs `WAIT_FOR_RELOAD`:
| Scenario | Action Type |
|----------|-------------|
| After clicking a navigation link | `WAIT_STABLE` |
| After form submission | `WAIT_STABLE` |
| After dropdown selection that triggers page refresh/postback | `WAIT_FOR_RELOAD` |
| After AJAX operation that reloads content | `WAIT_FOR_RELOAD` |
| After clicking button that stays on same URL but refreshes | `WAIT_FOR_RELOAD` |

#### Multi-Window Workflow Pattern:
```
1. CLICK_AND_SWITCH → Opens and switches to popup
2. WAIT_STABLE → Wait for popup to load
3. [... perform actions in popup ...]
4. CLOSE_WINDOW → Close popup and return to main window
```

### G. Selector Best Practices
- **PREFER Simple Selectors**: `#id`, `[name='x']`, `a[href*='keyword']`, `button:has-text('Text')`
- **AVOID Complex Selectors**: Nested `:has()`, `:near()`, `:nth-child()` chains
- **TEST Selector Complexity**: If a selector has more than 2 pseudo-selectors, simplify it
- **Fallback Strategy**: The framework tries multiple locators; provide at least `id`, `selector`, `cssSelector`, and `xpath`

    - **VERIFY_TEXT**: Include `"comparisonType": "CONTAINS"` or `"EXACTLY"`.
- **Parameterization**: 
    - Use `___RUNTIME_PARAMETER___` for values coming from Gherkin.
    - Use `RANDOM_FIRST_NAME`, `RANDOM_EMAIL`, etc., for fields requiring unique data.
    - Use `VAR_NAME` (e.g., `VAR_BOOKING_ID`) in `GET_TEXT` to capture values, and the same name in `TYPE` to reuse them.
- **Capture Logic**: When using `GET_TEXT`, the `value` field in JSON must contain the Variable Name (e.g., `"value": "VAR_BOOKING_ID"`).

## 3. Expected Outcomes
- **Comprehensive JSON**: A file in `src/test/resources/locatorRepository/` that follows the rigorous schema provided in the examples.
- **Null-Explicit Data**: Every one of the 17 attributes must be present. If an attribute is missing on the element, it must be explicitly set to `null` (never an empty string `""`).
- **Stable XPath**: A relative XPath that is shorter and more stable than a full absolute path, focusing on IDs and unique labels.

## 4. Don'ts (Critical Prohibitions)
- **NO Guesswork**: DO NOT generate JSON based on expected HTML structure. It **must** come from the live DOM during an active session.
- **NO Empty Strings**: DO NOT use `""` for missing attributes. Always use `null`.
- **NO Overwriting**: DO NOT overwrite an existing JSON file unless the user explicitly asks for a "Refresh" or "Update."
- **NO Generic Selectors**: DO NOT use weak locators like `nth-child` if stronger attributes (ID, Name, ARIA) are available.
- **NO Missing Attributes**: DO NOT skip any of the 17 mandatory attributes, even if the element is simple.
- **NO Extra Attributes**: DO NOT add attributes like `aria-label` (with hyphen). Use `ariaLabel` (camelCase) as per the schema. Unknown fields may cause deserialization issues.

## 5. Pre-Flight Checklist (Before Creating JSON)

Before creating a JSON file, verify:

| Check | Question | If Yes |
|-------|----------|--------|
| 🔗 **Popup Link?** | Does clicking this element open a new window/tab? | Use `CLICK_AND_SWITCH` + `WAIT_STABLE` |
| 🔄 **Page Refresh?** | Does the action cause the page to refresh (same URL)? | Add `WAIT_FOR_RELOAD` after the action |
| 🚀 **Navigation?** | Does clicking navigate to a different URL? | Add `WAIT_STABLE` after `CLICK` |
| 📋 **Dropdown Postback?** | Does selecting from dropdown refresh the page? | Add `WAIT_FOR_RELOAD` after `SELECT` |
| 🪟 **Multi-Window?** | Is this step in a popup window? | Ensure `CLICK_AND_SWITCH` was used to open it |
| 🔚 **Cleanup Needed?** | At end of popup workflow? | Add `CLOSE_WINDOW` to return to main |

## 6. Troubleshooting Common Issues

### Issue: Element Not Found
**Causes & Fixes:**
1. ❌ Page not fully loaded → Add `WAIT_STABLE` or `WAIT_FOR_RELOAD` before the step
2. ❌ Element in popup window → Use `CLICK_AND_SWITCH` to open popup first
3. ❌ Selector too complex → Simplify to `a[href*='keyword']` or `#id`
4. ❌ Wrong page context → Check URL in logs matches expected page

### Issue: Click Happened But Nothing Changed
**Causes & Fixes:**
1. ❌ Clicked wrong element → Use more specific selector or add `text` attribute
2. ❌ Element opens popup but used `CLICK` → Change to `CLICK_AND_SWITCH`
3. ❌ Page refresh needed → Add `WAIT_FOR_RELOAD` after the click

### Issue: Action Works Locally But Fails in Test
**Causes & Fixes:**
1. ❌ Timing issue → Add wait action (`WAIT_STABLE` or `WAIT_FOR_RELOAD`)
2. ❌ Page renders differently → Use multiple fallback selectors
3. ❌ Dynamic content → Wait for specific element, not just page load

## 7. Example JSON Outcome: `login_to_account.json`

```json
{
  "stepFileName": "login_to_account",
  "gherkinStep": "Enter username \"student\" and password \"Password123\" and click login",
  "normalizedStep": "enter_username_param_and_password_param_and_click_login",
  "stepType": "When",
  "stepNumber": 1,
  "status": "passed",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "TYPE",
      "description": "Enter Username",
      "element": {
        "type": "input",
        "id": "username",
        "name": "username",
        "selector": "#username",
        "cssSelector": "input#username",
        "xpath": "//input[@id='username']",
        "text": null,
        "placeholder": "Enter your username",
        "dataTest": "user-login-field",
        "ariaLabel": "Username Field",
        "role": "textbox",
        "title": null,
        "alt": null,
        "className": "input-field login-input",
        "value": null,
        "href": null,
        "src": null,
        "coordinates": null,
        "fingerprint": {
          "attributes": { "type": "text", "role": "textbox" },
          "context": { "nearbyText": "Username", "parentTag": "div", "heading": "Login Area" }
        }
      },
      "value": "___RUNTIME_PARAMETER___"
    },
    {
      "actionNumber": 2,
      "actionType": "TYPE",
      "description": "Enter Password",
      "element": {
        "type": "input",
        "id": "password",
        "name": "password",
        "selector": "#password",
        "cssSelector": "input#password",
        "xpath": "//input[@id='password']",
        "text": null,
        "placeholder": "Enter your password",
        "dataTest": "user-pass-field",
        "ariaLabel": "Password Field",
        "role": "textbox",
        "className": "input-field login-input",
        "fingerprint": {
          "attributes": { "type": "password", "role": "textbox" },
          "context": { "nearbyText": "Password", "parentTag": "div", "heading": "Login Area" }
        }
      },
      "value": "___RUNTIME_PARAMETER___"
    },
    {
    "actionNumber" : 3,
    "actionType" : "SCREENSHOT",
    "description" : "LoginPage Screenshot"
  },
    {
      "actionNumber": 4,
      "actionType": "CLICK",
      "description": "Click Login Button",
      "element": {
        "type": "button",
        "id": "submit",
        "name": null,
        "selector": "button:has-text('Login')",
        "cssSelector": "button.btn-submit",
        "xpath": "//button[@id='submit']",
        "text": "Login",
        "placeholder": null,
        "dataTest": null,
        "ariaLabel": null,
        "role": "button",
        "title": "Login to your account",
        "alt": null,
        "className": "btn-submit btn-primary",
        "value": "Submit",
        "href": null,
        "src": null,
        "coordinates": null,
        "fingerprint": {
          "attributes": { "type": "submit", "role": "button" },
          "context": { "nearbyText": "Forgot Password?", "parentTag": "form", "heading": "Login Area" }
        }
      }
    }
  ],
  "metadata": {
    "createdDate": "2026-01-11T11:32:00",
    "totalActions": 3
  }
}
```

## 8. Example: Popup Window Workflow

### Step 1: `click_on_admin_and_switch_to_new_window.json`
```json
{
  "stepFileName": "click_on_admin_and_switch_to_new_window",
  "gherkinStep": "click on Admin and switch to new window",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "CLICK_AND_SWITCH",
      "description": "Click Admin link and switch to popup window",
      "element": {
        "type": "a",
        "selector": "a:has-text('Admin')",
        "text": "Admin",
        "href": "/admin"
      }
    },
    {
      "actionNumber": 2,
      "actionType": "WAIT_STABLE",
      "description": "Wait for popup window to fully load"
    }
  ]
}
```

### Step 2: `select_param_from_dropdown.json` (with page refresh)
```json
{
  "stepFileName": "select_param_from_dropdown",
  "gherkinStep": "select \"<value>\" from dropdown",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "SELECT",
      "description": "Select value from dropdown",
      "element": {
        "type": "select",
        "id": "clientDropdown",
        "selector": "#clientDropdown"
      },
      "value": "___RUNTIME_PARAMETER___"
    },
    {
      "actionNumber": 2,
      "actionType": "WAIT_FOR_RELOAD",
      "description": "Wait for page to reload after selection",
      "timeout": 30000
    }
  ]
}
```

### Step 3: `close_window_and_return_to_main.json`
```json
{
  "stepFileName": "close_window_and_return_to_main",
  "gherkinStep": "Close the current window and switch to main window",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "CLOSE_WINDOW",
      "description": "Close popup and return to main window"
    }
  ]
}
```
