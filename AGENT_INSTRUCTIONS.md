# AI Agent Protocol: JSON Repository Automation

## 1. Objective
Build and maintain a **"Strong" JSON-based Locator Repository** that serves as the single source of truth for the automation framework. This repository must contain comprehensive, verified element metadata captured from a **live browser session** to enable high-performance execution and robust AI self-healing.

## 2. Mandatory Requirements

### A. Pre-Execution Check
- **Normalize Step**: Convert the Gherkin step into a filename-safe format (Lowercase, spaces to `_`, and replace specific values/digits with `_param_`).
- **Verify Existence**: Always check if `src/test/resources/locatorRepository/[normalized_name].json` already exists. If it exists, **stop** and inform the user; do not duplicate data.

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
- **Specific ActionTypes**: Map every action to these exact types: `NAVIGATE`, `CLICK`, `DOUBLE_CLICK`, `RIGHT_CLICK`, `TYPE`, `CLEAR`, `SELECT`, `HOVER`, `CHECK`, `UNCHECK`, `PRESS_KEY`, `SWITCH_WINDOW`, `DRAG_DROP`, `SCROLL`, `WAIT_NAVIGATION`, `VERIFY_TEXT`, `VERIFY_ELEMENT`, `SCREENSHOT`.
- **Parameterization**: Use `___RUNTIME_PARAMETER___` for action values and `_param_` for normalized step names.

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

## 5. Example JSON Outcome: `login_to_account.json`

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
