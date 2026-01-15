# Framework Recreation Guide

## 1. Framework Overview

### 1.1 Framework Name
**JSON-Driven No-Code Playwright Automation Framework**

### 1.2 Core Philosophy
A hybrid test automation framework that enables **true no-code automation** through JSON-based locator repositories while maintaining **intelligent fallback** to traditional coded step definitions. The framework combines AI-assisted self-healing with Cucumber BDD for maximum maintainability and resilience.

### 1.3 Technology Stack
- **Programming Language**: Java 21
- **Browser Automation**: Playwright 1.57.0
- **BDD Framework**: Cucumber 7.15.0
- **Test Runner**: TestNG 7.9.0
- **Dependency Injection**: PicoContainer (Cucumber)
- **Reporting**: Allure 2.27.0
- **Build Tool**: Maven 3.6+
- **Logging**: SLF4J + Logback

---

## 2. Architecture & Design Patterns

### 2.1 Architectural Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    GHERKIN FEATURE FILES                     │
│                  (Business-Readable Tests)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│              UNIVERSAL STEP DEFINITION                       │
│         (Single Entry Point - catches ALL steps)             │
└──────────────────────────┬──────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
┌───────────────────▼───┐    ┌────▼──────────────────────┐
│  JSON REPOSITORY       │    │  TRADITIONAL STEPS        │
│  (Primary Path)        │    │  (Fallback Path)          │
└───────────┬────────────┘    └───────────────────────────┘
            │
┌───────────▼────────────────────────────────────────────────┐
│              JSON ENHANCED EXECUTOR                         │
│         (Orchestrates action execution)                     │
└───────────┬────────────────────────────────────────────────┘
            │
    ┌───────┴──────────┬──────────────┬─────────────┐
    │                  │              │             │
┌───▼────────┐  ┌──────▼─────┐  ┌────▼──────┐  ┌──▼──────┐
│ Navigation │  │Interaction │  │Verification│  │Screenshot│
│  Handler   │  │  Handler   │  │  Handler   │  │  Action │
└────┬───────┘  └──────┬─────┘  └────┬───────┘  └──┬──────┘
     │                 │              │             │
     └─────────────────┴──────────────┴─────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│              SMART LOCATOR FINDER                            │
│         (Orchestrates locator resolution)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
            ┌──────────┴──────────┐
            │                     │
┌───────────▼──────────┐   ┌──────▼─────────────────────┐
│  LOCATOR STRATEGY    │   │  SELF-HEALING ENGINE       │
│  (Fallback Priority) │   │  (Fingerprint-based)       │
└──────────────────────┘   └────────────────────────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
            ┌───────▼────────┐          ┌─────────▼────────┐
            │ Label Healing  │          │ Semantic Healing │
            └────────────────┘          └──────────────────┘
                    │                             │
            ┌───────▼────────┐          ┌─────────▼────────┐
            │Proximity Heal  │          │ Fuzzy Attr Heal  │
            └────────────────┘          └──────────────────┘
```

### 2.2 Design Patterns Used

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **Repository Pattern** | `StepRepository.java` | Centralized JSON storage/retrieval for steps |
| **Strategy Pattern** | `LocatorStrategy.java` | Multiple locator resolution strategies |
| **Chain of Responsibility** | `SelfHealingEngine.java` | Cascading healing strategies |
| **Handler Pattern** | `NavigationHandler`, `InteractionHandler`, `VerificationHandler` | Specialized action execution |
| **Singleton** | `PlaywrightManager.java` | Single browser instance management |
| **Dependency Injection** | PicoContainer | Sharing context across step definitions |

---

## 3. Core Components & Classes

### 3.1 Entry Point & Orchestration

#### **Class: `UniversalStepDefinition.java`**
**Package**: `com.framework.cucumber`

**Purpose**: Single entry point that intercepts ALL Gherkin steps

**Key Methods**:
```java
@Given("^(.*)$")
@When("^(.*)$") 
@Then("^(.*)$")
public void executeStep(String step)
```

**Execution Logic**:
1. Receive Gherkin step text
2. Check if JSON exists in `locatorRepository/`
3. If JSON found → delegate to `JsonEnhancedExecutor`
4. If JSON NOT found → fall back to traditional Java step definitions
5. Log execution path for transparency

**Critical Features**:
- `@Before` hook: Initialize browser, set scenario context, add metadata tags
- `@After` hook: Capture screenshots on failure, attach videos, close browser
- Thread-safe scenario management via `ThreadLocal<Scenario>`

---

#### **Class: `JsonEnhancedExecutor.java`**
**Package**: `com.framework.executor`

**Purpose**: Executes steps using JSON-provided locator data

**Key Method**:
```java
public void executeStep(String gherkinStep)
```

**Workflow**:
1. Load step JSON from `StepRepository`
2. Extract `actions[]` array
3. For each action:
   - Determine action type (CLICK, TYPE, VERIFY, etc.)
   - Delegate to appropriate handler
   - Pass runtime parameters extracted from Gherkin step
4. Log each action with details

**Handler Delegation**:
- `NAVIGATE` → `NavigationHandler.executeNavigate()`
- `CLICK`, `TYPE`, `SELECT`, etc. → `InteractionHandler.execute*`
- `VERIFY_TEXT`, `VERIFY_ELEMENT` → `VerificationHandler.execute*`
- `SCREENSHOT` → Direct Playwright screenshot

---

### 3.2 Data Layer

#### **Class: `StepRepository.java`**
**Package**: `com.framework.data`

**Purpose**: CRUD operations for JSON step files

**Directory Structure**:
```
src/test/resources/locatorRepository/
├── user_enters_username_param_and_password_param.json
├── user_clicks_login_button.json
└── validate_selected_client_should_display_on_header.json
```

**Key Methods**:

| Method | Purpose |
|--------|---------|
| `stepExists(String gherkinStep)` | Check if JSON file exists |
| `getStep(String gherkinStep)` | Load StepData from JSON |
| `saveStep(StepData stepData)` | Write JSON to file |
| `getActions(String gherkinStep)` | Get action list from JSON |
| `generateStepFileName(String gherkinStep)` | Normalize step to filename |
| `populateLiveAttributes(Locator element, ElementLocators locators)` | Enrich JSON with 17 attributes from live DOM |

**File Naming Convention**:
- Convert to lowercase
- Replace spaces with `_`
- Replace specific values/numbers with `_param_`
- Example: `"User enters username 'admin'"` → `user_enters_username_param.json`

---

#### **Class: `StepData.java`**
**Package**: `com.framework.data`

**Purpose**: Model for JSON step structure

**Structure**:
```java
public class StepData {
    private String stepFileName;
    private String gherkinStep;
    private String normalizedStep;
    private String stepType;        // Given/When/Then
    private int stepNumber;
    private String status;          // passed/failed
    private List<ActionData> actions;
    private Metadata metadata;
}
```

---

#### **Class: `ActionData.java`**
**Package**: `com.framework.data`

**Purpose**: Model for individual action within a step

**Structure**:
```java
public class ActionData {
    private int actionNumber;
    private String actionType;      // CLICK, TYPE, VERIFY_TEXT, etc.
    private String description;
    private ElementLocators element;
    private String value;           // For TYPE/SELECT actions
    private String targetElement;   // For DRAG_DROP
}
```

**Supported Action Types**:
```
NAVIGATE, CLICK, DOUBLE_CLICK, RIGHT_CLICK, TYPE, CLEAR, 
SELECT, HOVER, CHECK, UNCHECK, PRESS_KEY, SWITCH_WINDOW, 
DRAG_DROP, SCROLL, WAIT_NAVIGATION, VERIFY_TEXT, 
VERIFY_ELEMENT, SCREENSHOT
```

---

#### **Class: `ElementLocators.java`**
**Package**: `com.framework.data`

**Purpose**: The "Strong JSON" identity - 17 attributes + fingerprint

**The 17 Mandatory Attributes**:
```java
public class ElementLocators {
    // Core Identifiers
    private String type;            // Tag name (input, button, etc.)
    private String id;
    private String name;
    private String selector;        // Playwright selector
    private String cssSelector;
    private String xpath;           // Relative XPath
    
    // Content Attributes
    private String text;
    private String placeholder;
    private String value;
    
    // Accessibility Attributes
    private String dataTest;
    private String ariaLabel;
    private String role;
    private String title;
    private String alt;
    
    // Visual Attributes
    private String className;
    private String href;            // For links
    private String src;             // For images
    
    // Healing Data
    private FingerprintData fingerprint;
    private boolean isHealed;
}
```

**Fingerprint Structure**:
```java
public static class FingerprintData {
    private Attributes attributes;  // role, type, aria-label
    private Context context;        // nearbyText, parentTag, heading
}

public static class Attributes {
    private String type;
    private String role;
    private String ariaLabel;
    private String classList;
}

public static class Context {
    private String nearbyText;
    private String parentTag;
    private String heading;
}
```

---

### 3.3 Locator Resolution

#### **Class: `SmartLocatorFinder.java`**
**Package**: `com.framework.healing`

**Purpose**: Orchestrates standard locators + self-healing

**Execution Flow**:
```
1. Wait for element with best locator (5s timeout)
2. Try LocatorStrategy.findElementWithFallback()
   └─ If found → Enrich with live attributes → Return
3. If NOT found → Invoke SelfHealingEngine
   └─ If healed → Update JSON with new selector → Return
4. If all fail → Return null (test fails)
```

**Post-Healing Enrichment**:
- Captures "Before" state (17 attributes)
- Captures "After" state (17 attributes)
- Logs comprehensive healing report showing what changed

---

#### **Class: `LocatorStrategy.java`**
**Package**: `com.framework.strategy`

**Purpose**: Prioritized locator fallback mechanism

**Priority Order**:
```
1. ID              → #username
2. Name            → input[name="username"]
3. CSS Selector    → .login-input
4. Selector        → getByRole, getByLabel, etc.
5. XPath           → //input[@id='username']
6. Text            → getByText("Login")
7. Placeholder     → getByPlaceholder("Enter username")
8. Data-testid     → [data-testid="login-btn"]
```

**Method**:
```java
public static Locator findElementWithFallback(Page page, ElementLocators locators)
```

**Logic**:
- Build list of `LocatorAttempt` objects
- Try each locator in priority order
- Check `locator.count() > 0`
- Return first working locator
- Log which strategy succeeded

---

#### **Class: `SelfHealingEngine.java`**
**Package**: `com.framework.healing`

**Purpose**: AI-powered element recovery using fingerprint DNA

**Healing Strategies** (in order):

| Strategy | Method | Logic |
|----------|--------|-------|
| **1. Label Healing** | `tryLabelHeal()` | Use `page.getByLabel(nearbyText)` |
| **2. Semantic Healing** | `trySemanticHeal()` | Use `page.getByRole(role, {name: ariaLabel})` |
| **3. Proximity Healing** | `tryProximityHeal()` | Use `:near()` relative selector |
| **4. Fuzzy Attribute** | `tryFuzzyAttributeHeal()` | Match by unique class name |

**Return Type**:
```java
public static class HealedResult {
    public final Locator locator;
    public final String selector;
}
```

**When Triggered**:
- Only when ALL standard locators fail
- Requires `fingerprint` object in JSON
- Logs comprehensive before/after report

---

### 3.4 Action Handlers

#### **Class: `NavigationHandler.java`**
**Package**: `com.framework.executor`

**Key Methods**:
```java
public static void executeNavigate(Page page, ActionData action, String originalGherkinStep)
```

**Logic**:
- Extract URL from action.value or resolve from Gherkin using `UrlResolver`
- Navigate using `page.navigate(url)`
- Wait for page load completion
- Log navigation

---

#### **Class: `InteractionHandler.java`**
**Package**: `com.framework.executor`

**Key Methods**:

| Method | Action Type | Implementation |
|--------|------------|----------------|
| `executeClick()` | CLICK | Find element → `locator.click()` |
| `executeType()` | TYPE | Extract param → `locator.fill(value)` |
| `executeSelect()` | SELECT | Determine if date/dropdown → delegate |
| `executeSelectDropdown()` | SELECT | `locator.selectOption(value)` |
| `executeSelectDate()` | SELECT | `locator.fill(dateValue)` using `DateResolver` |
| `executeHover()` | HOVER | `locator.hover()` |
| `executeCheck()` | CHECK | `locator.check()` |
| `executeUncheck()` | UNCHECK | `locator.uncheck()` |
| `executePressKey()` | PRESS_KEY | `page.keyboard().press(key)` |
| `executeDragDrop()` | DRAG_DROP | `sourceLocator.dragTo(targetLocator)` |

**Common Pattern**:
```java
1. Find element using SmartLocatorFinder.findElement()
2. Extract runtime parameter from Gherkin (if needed)
3. Perform action
4. Log action with effective selector
5. Handle errors gracefully
```

---

#### **Class: `VerificationHandler.java`**
**Package**: `com.framework.executor`

**Key Methods**:

| Method | Verification Type | Logic |
|--------|------------------|-------|
| `executeVerifyText()` | VERIFY_TEXT | Check `locator.textContent()` matches expected |
| `executeVerifyElement()` | VERIFY_ELEMENT | Check `locator.isVisible()` |

**Assertions**:
- Uses TestNG assertions for failure reporting
- Logs verification status
- Captures screenshots on verification

---

### 3.5 Utility Classes

#### **Class: `ParameterExtractor.java`**
**Package**: `com.framework.utils`

**Purpose**: Extract quoted parameters from Gherkin steps

**Method**:
```java
public static String extractParameter(String gherkinStep, int index)
```

**Example**:
```
Gherkin: Enter username "admin" and password "Test@123"
extractParameter(step, 0) → "admin"
extractParameter(step, 1) → "Test@123"
```

---

#### **Class: `DateResolver.java`**
**Package**: `com.framework.utils`

**Purpose**: Convert relative date keywords to actual dates

**Supported Keywords**:
- `TODAY` → Current date
- `TOMORROW` → Today + 1 day
- `YESTERDAY` → Today - 1 day
- `NEXT_WEEK` → Today + 7 days
- Specific formats: `dd-MMM-yyyy`, `MM/dd/yyyy`

---

#### **Class: `UrlResolver.java`**
**Package**: `com.framework.utils`

**Purpose**: Resolve environment-specific URLs from Gherkin

**Method**:
```java
public static String resolveUrl(String urlFragment, String gherkinStep)
```

**Logic**:
- Read base URL from `env/{environment}/config.properties`
- If Gherkin contains full URL → use it
- If Gherkin contains fragment → append to base URL

---

#### **Class: `PlaywrightManager.java`**
**Package**: `com.framework.playwright`

**Purpose**: Singleton browser lifecycle management

**Key Methods**:

| Method | Purpose |
|--------|---------|
| `getPage()` | Get current page instance |
| `initializeBrowser()` | Launch browser with config |
| `closeBrowser()` | Close browser and context |

**Browser Configuration**:
```java
BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
    .setHeadless(ExecutionConfig.isHeadless())
    .setSlowMo(ExecutionConfig.getSlowMotion());

Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
    .setViewportSize(1920, 1080)
    .setRecordVideoDir(Paths.get("reports/videos"))
    .setRecordVideoSize(1920, 1080);
```

---

### 3.6 Configuration Classes

#### **Class: `ExecutionConfig.java`**
**Package**: `com.framework.config`

**Purpose**: Runtime execution settings

**Properties**:
```java
public static String getBrowserName()      // chromium/firefox/webkit
public static boolean isHeadless()         // true on Linux
public static int getSlowMotion()          // Delay between actions (ms)
public static int getDefaultTimeout()      // 30000ms
```

**Source**: System properties or defaults

---

#### **Class: `EnvironmentConfig.java`**
**Package**: `com.framework.config`

**Purpose**: Environment-specific configuration

**Directory Structure**:
```
src/test/resources/env/
├── dev/
│   └── config.properties
├── staging/
│   └── config.properties
└── prod/
    └── config.properties
```

**Properties**:
```properties
base.url=https://example.com
api.key=xyz123
```

---

### 3.7 Reporting

#### **Class: `ErrorReporter.java`**
**Package**: `com.framework.reporting`

**Purpose**: Allure report annotations and attachments

**Key Methods**:
```java
public static void logStep(String description, String status)
public static void attachScreenshot(byte[] screenshot)
public static void attachText(String name, String content)
```

**Allure Integration**:
- Uses `@Step`, `@Attachment` annotations
- Captures screenshots on failure
- Attaches video recordings
- Adds environment metadata

---

## 4. JSON Repository Schema

### 4.1 Complete JSON Example

```json
{
  "stepFileName": "user_enters_username_param_and_password_param",
  "gherkinStep": "User enters username \"admin\" and password \"Test@123\"",
  "normalizedStep": "user_enters_username_param_and_password_param",
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
        "fingerprint": {
          "attributes": {
            "type": "text",
            "role": "textbox",
            "ariaLabel": "Username Field",
            "classList": "input-field login-input"
          },
          "context": {
            "nearbyText": "Username",
            "parentTag": "div",
            "heading": "Login Area"
          }
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
        "title": null,
        "alt": null,
        "className": "input-field login-input",
        "value": null,
        "href": null,
        "src": null,
        "fingerprint": {
          "attributes": {
            "type": "password",
            "role": "textbox",
            "ariaLabel": "Password Field",
            "classList": "input-field login-input"
          },
          "context": {
            "nearbyText": "Password",
            "parentTag": "div",
            "heading": "Login Area"
          }
        }
      },
      "value": "___RUNTIME_PARAMETER___"
    }
  ],
  "metadata": {
    "createdDate": "2026-01-11T11:32:00",
    "totalActions": 2
  }
}
```

### 4.2 Attribute Capture Rules

| Attribute | Source | If Not Present |
|-----------|--------|----------------|
| `type` | `element.tagName` | `null` |
| `id` | `element.id` | `null` |
| `name` | `element.getAttribute('name')` | `null` |
| `selector` | Playwright's best selector | Required |
| `cssSelector` | Constructed CSS | `null` |
| `xpath` | Relative XPath from nearest ID | `null` |
| `text` | `element.textContent` | `null` |
| `placeholder` | `element.placeholder` | `null` |
| `dataTest` | `element.getAttribute('data-testid')` | `null` |
| `ariaLabel` | `element.ariaLabel` | `null` |
| `role` | `element.role` | `null` |
| `title` | `element.title` | `null` |
| `alt` | `element.alt` | `null` |
| `className` | `element.className` | `null` |
| `value` | `element.value` | `null` |
| `href` | `element.href` | `null` |
| `src` | `element.src` | `null` |

**Critical Rule**: NEVER use empty string `""`, always use `null` for missing attributes

---

## 5. Execution Flow

### 5.1 Test Execution Sequence

```
1. TestNG Runner (RunCucumberTest.java) starts
   └─ Discovers feature files in src/test/resources/features/
   └─ Applies tag filter (@sanity, @login, etc.)

2. Cucumber parses feature file
   └─ For each scenario:
       └─ Trigger @Before hook (UniversalStepDefinition)
           └─ Initialize browser
           └─ Set scenario context
           └─ Add metadata tags to Allure

3. For each Gherkin step:
   └─ Call UniversalStepDefinition.executeStep(step)
       └─ Generate step filename from Gherkin text
       └─ Check if JSON exists:
           ├─ YES → JsonEnhancedExecutor.executeStep()
           │   └─ Load StepData from JSON
           │   └─ For each action in actions[]:
           │       └─ Delegate to handler
           │           └─ SmartLocatorFinder.findElement()
           │               ├─ LocatorStrategy (standard)
           │               └─ SelfHealingEngine (if failed)
           │           └─ Perform action
           │           └─ Log result
           │
           └─ NO → Fall back to traditional step definitions
               └─ Scan com.framework.steps package
               └─ Execute matching @Given/@When/@Then method

4. After scenario completes:
   └─ Trigger @After hook
       └─ If failed → Capture screenshot
       └─ Attach video to Allure
       └─ Close browser
       └─ Log scenario result

5. After all tests:
   └─ Generate Allure report
   └─ Open in browser
```

---

### 5.2 Action Execution Detail

```
JsonEnhancedExecutor.executeAction(action):
├─ Switch on actionType:
│   ├─ NAVIGATE:
│   │   └─ NavigationHandler.executeNavigate()
│   │       └─ Resolve URL
│   │       └─ page.navigate(url)
│   │       └─ Wait for load state
│   │
│   ├─ CLICK:
│   │   └─ InteractionHandler.executeClick()
│   │       └─ SmartLocatorFinder.findElement()
│   │       └─ locator.click()
│   │
│   ├─ TYPE:
│   │   └─ InteractionHandler.executeType()
│   │       └─ SmartLocatorFinder.findElement()
│   │       └─ ParameterExtractor.extractParameter()
│   │       └─ locator.fill(value)
│   │
│   ├─ SELECT:
│   │   └─ InteractionHandler.executeSelect()
│   │       └─ Determine if date or dropdown
│   │       ├─ Date → executeSelectDate()
│   │       │   └─ DateResolver.resolve()
│   │       │   └─ locator.fill(date)
│   │       └─ Dropdown → executeSelectDropdown()
│   │           └─ locator.selectOption(value)
│   │
│   ├─ VERIFY_TEXT:
│   │   └─ VerificationHandler.executeVerifyText()
│   │       └─ SmartLocatorFinder.findElement()
│   │       └─ Assert.assertEquals(actual, expected)
│   │
│   └─ SCREENSHOT:
│       └─ page.screenshot()
│       └─ Save to reports/screenshots/
│       └─ Attach to Allure
```

---

### 5.3 Element Finding Flow

```
SmartLocatorFinder.findElement(page, locators):
├─ Step 1: Wait for best locator (5s)
│   └─ page.locator(bestLocator).waitFor(ATTACHED, 5000)
│
├─ Step 2: Try Standard Fallback
│   └─ LocatorStrategy.findElementWithFallback()
│       └─ For each locator in priority order:
│           ├─ Try ID
│           ├─ Try Name
│           ├─ Try CSS Selector
│           ├─ Try Selector
│           ├─ Try XPath
│           ├─ Try Text
│           ├─ Try Placeholder
│           └─ Try Data-testid
│       └─ Return first successful locator
│
├─ If found:
│   └─ StepRepository.populateLiveAttributes()
│   └─ Return locator
│
└─ If NOT found:
    └─ Step 3: Self-Healing Engine
        ├─ Capture "Before" state (17 attributes)
        ├─ SelfHealingEngine.attemptHealing()
        │   ├─ Strategy 1: tryLabelHeal()
        │   ├─ Strategy 2: trySemanticHeal()
        │   ├─ Strategy 3: tryProximityHeal()
        │   └─ Strategy 4: tryFuzzyAttributeHeal()
        ├─ If healed:
        │   ├─ Update locators.selector with new selector
        │   ├─ Set locators.isHealed = true
        │   ├─ StepRepository.populateLiveAttributes()
        │   ├─ Capture "After" state
        │   ├─ Log comprehensive healing report
        │   └─ Return healed locator
        └─ If healing failed:
            └─ Return null (test will fail)
```

---

## 6. Strategy & Principles

### 6.1 No-Code Strategy

**Primary Path**: JSON-driven execution
- Business analysts write Gherkin
- Automation engineer captures JSON once
- JSON is reused across all scenarios
- No coding required for 90% of tests

**Example**:
```gherkin
Feature: Login Flow
  Scenario: Successful Login
    Given User navigates to "https://example.com"
    When User enters username "admin" and password "Test@123"
    And User clicks login button
    Then User should see "Welcome, Admin"
```

**JSON Files Required**:
1. `user_navigates_to_param.json`
2. `user_enters_username_param_and_password_param.json`
3. `user_clicks_login_button.json`
4. `user_should_see_param.json`

**No Java Code Needed** - framework handles everything!

---

### 6.2 Fallback Strategy

**When JSON doesn't exist**:
1. Framework scans `com.framework.steps` package
2. Finds `@Given/@When/@Then` method matching step text
3. Executes traditional step definition
4. Logs: "Using Traditional Step Definition"

**Example Traditional Step**:
```java
@When("User performs complex calculation with {int} and {int}")
public void performComplexCalculation(int a, int b) {
    // Custom Java logic here
    int result = a * b + Math.sqrt(a);
    // ...
}
```

---

### 6.3 Self-Healing Strategy

**Philosophy**: Tests should self-repair when UI changes

**Trigger Condition**: All standard locators fail

**Healing Process**:
1. Use fingerprint metadata (role, aria-label, nearby text)
2. Try 4 strategies in sequence
3. If found, update JSON with new selector
4. Log comprehensive report
5. Continue test execution

**Example Healing Report**:
```
====================================================
           COMPREHENSIVE HEALING REPORT             
====================================================
 ATTRIBUTE       | BEFORE (BROKEN)    | AFTER (HEALED)     
----------------------------------------------------
 ID              | submit             | login-btn          
 Selector        | #submit            | #login-btn         
 XPath           | //button[@id='...  | //button[@id='...  
====================================================
```

---

### 6.4 Parameterization Strategy

**Quoted Parameters in Gherkin**:
```gherkin
When User enters username "admin" and password "Test@123"
```

**JSON Value**:
```json
"value": "___RUNTIME_PARAMETER___"
```

**Extraction**:
```java
String username = ParameterExtractor.extractParameter(gherkinStep, 0); // "admin"
String password = ParameterExtractor.extractParameter(gherkinStep, 1); // "Test@123"
```

---

### 6.5 Strong JSON Strategy

**17 Attributes = Maximum Resilience**

**Why 17?**
- Multiple locators → Higher success rate
- Fingerprint DNA → Enables self-healing
- Live enrichment → Always up-to-date

**Capture Method**:
```java
StepRepository.populateLiveAttributes(locator, elementLocators)
```

**JavaScript Evaluation**:
```javascript
element.evaluate(el => ({
    type: el.tagName,
    id: el.id || null,
    name: el.getAttribute('name') || null,
    text: el.textContent || null,
    // ... all 17 attributes
}))
```

---

## 7. Maven Configuration

### 7.1 POM.xml Structure

**Key Dependencies**:
```xml
<dependencies>
    <!-- Playwright -->
    <dependency>
        <groupId>com.microsoft.playwright</groupId>
        <artifactId>playwright</artifactId>
        <version>1.57.0</version>
    </dependency>
    
    <!-- Cucumber -->
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <version>7.15.0</version>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-testng</artifactId>
        <version>7.15.0</version>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-picocontainer</artifactId>
        <version>7.15.0</version>
    </dependency>
    
    <!-- TestNG -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.9.0</version>
    </dependency>
    
    <!-- Allure -->
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-cucumber7-jvm</artifactId>
        <version>2.27.0</version>
    </dependency>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-testng</artifactId>
        <version>2.27.0</version>
    </dependency>
</dependencies>
```

**Build Plugins**:
```xml
<build>
    <plugins>
        <!-- Compiler -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.12.1</version>
            <configuration>
                <source>21</source>
                <target>21</target>
            </configuration>
        </plugin>
        
        <!-- Surefire (Test Execution) -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.3</version>
            <configuration>
                <parallel>${parallel.mode}</parallel>
                <threadCount>${ThreadCount}</threadCount>
                <systemPropertyVariables>
                    <cucumber.filter.tags>${Tags}</cucumber.filter.tags>
                </systemPropertyVariables>
            </configuration>
        </plugin>
        
        <!-- Allure Reporting -->
        <plugin>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-maven</artifactId>
            <version>2.12.0</version>
            <configuration>
                <reportVersion>2.27.0</reportVersion>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

### 7.2 Maven Commands

| Command | Purpose |
|---------|---------|
| `mvn clean test` | Run all tests |
| `mvn test -DTags="@sanity"` | Run specific tags |
| `mvn test -DThreadCount=4` | Parallel execution |
| `mvn allure:serve` | Generate and view report |
| `mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"` | Install browsers |

---

## 8. TestNG Runner Configuration

### 8.1 RunCucumberTest.java

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.framework.cucumber",
    plugin = {
        "pretty",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        "json:target/cucumber-reports/cucumber.json",
        "html:target/cucumber-reports/cucumber.html"
    },
    monochrome = true,
    dryRun = false
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

---

## 9. Directory Structure

```
jsonplaywright/
├── pom.xml
├── README.md
├── AGENT_INSTRUCTIONS.md
├── FRAMEWORK_RECREATION_GUIDE.md
├── .gitignore
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── framework/
│   │               ├── config/
│   │               │   ├── EnvironmentConfig.java
│   │               │   ├── ExecutionConfig.java
│   │               │   └── ParallelExecutionConfig.java
│   │               ├── cucumber/
│   │               │   └── UniversalStepDefinition.java
│   │               ├── data/
│   │               │   ├── ActionData.java
│   │               │   ├── ElementLocators.java
│   │               │   ├── FeatureData.java
│   │               │   ├── JsonDataProvider.java
│   │               │   ├── SharedStepsProvider.java
│   │               │   ├── StepData.java
│   │               │   └── StepRepository.java
│   │               ├── executor/
│   │               │   ├── InteractionHandler.java
│   │               │   ├── JsonEnhancedExecutor.java
│   │               │   ├── NavigationHandler.java
│   │               │   └── VerificationHandler.java
│   │               ├── healing/
│   │               │   ├── SelfHealingEngine.java
│   │               │   └── SmartLocatorFinder.java
│   │               ├── playwright/
│   │               │   ├── PlaywrightActions.java
│   │               │   └── PlaywrightManager.java
│   │               ├── reporting/
│   │               │   └── ErrorReporter.java
│   │               ├── steps/
│   │               │   └── TraditionalSteps.java
│   │               ├── strategy/
│   │               │   ├── LocatorStrategy.java
│   │               │   ├── RetryConfig.java
│   │               │   └── SmartWaitStrategy.java
│   │               └── utils/
│   │                   ├── DateResolver.java
│   │                   ├── ParameterExtractor.java
│   │                   ├── StepDiscoveryRegistry.java
│   │                   ├── StepLibraryManager.java
│   │                   ├── StepRepositoryPopulator.java
│   │                   ├── TestRunnerUtils.java
│   │                   └── UrlResolver.java
│   │
│   └── test/
│       ├── java/
│       │   ├── examples/
│       │   │   ├── AutoRecorder.java
│       │   │   └── ChromeRecorderConverter.java
│       │   └── runner/
│       │       └── RunCucumberTest.java
│       │
│       └── resources/
│           ├── features/
│           │   └── Sample.feature
│           ├── locatorRepository/
│           │   ├── user_enters_username_param_and_password_param.json
│           │   ├── user_clicks_login_button.json
│           │   └── validate_selected_client_should_display_on_header.json
│           ├── env/
│           │   ├── dev/
│           │   │   └── config.properties
│           │   └── staging/
│           │       └── config.properties
│           └── allure.properties
│
├── allure-results/         # Generated during test execution
├── reports/                # Timestamped execution reports
├── logs/                   # Execution logs
└── target/                 # Maven build output
```

---

## 10. Language Migration Guide

### 10.1 Core Concepts to Preserve

**Regardless of language, maintain**:
1. Single universal step definition entry point
2. JSON repository structure (17 attributes)
3. Locator fallback priority
4. Self-healing with fingerprint DNA
5. Handler-based action delegation
6. Parameter extraction from Gherkin

---

### 10.2 Python Implementation

**Technology Stack**:
- **Browser**: Playwright for Python
- **BDD**: Behave (Gherkin)
- **Runner**: Pytest or Behave runner
- **Reporting**: Allure Behave

**Key Classes**:
```python
# universal_step_definition.py
@given(step_matcher="re", pattern=r"^(.*)$")
@when(step_matcher="re", pattern=r"^(.*)$")
@then(step_matcher="re", pattern=r"^(.*)$")
def execute_step(context, step_text):
    json_executor.execute_step(step_text)

# step_repository.py
class StepRepository:
    @staticmethod
    def step_exists(gherkin_step: str) -> bool:
        # ...
    
    @staticmethod
    def get_step(gherkin_step: str) -> StepData:
        # ...

# locator_strategy.py
class LocatorStrategy:
    @staticmethod
    def find_element_with_fallback(page: Page, locators: ElementLocators) -> Locator:
        # Priority: id > name > css > xpath > text > placeholder
```

---

### 10.3 C# Implementation

**Technology Stack**:
- **Browser**: Playwright for .NET
- **BDD**: SpecFlow (Gherkin)
- **Runner**: NUnit or xUnit
- **Reporting**: Allure SpecFlow

**Key Classes**:
```csharp
// UniversalStepDefinition.cs
[Given(@"^(.*)$")]
[When(@"^(.*)$")]
[Then(@"^(.*)$")]
public void ExecuteStep(string stepText)
{
    _jsonExecutor.ExecuteStep(stepText);
}

// StepRepository.cs
public class StepRepository
{
    public static bool StepExists(string gherkinStep) { }
    public static StepData GetStep(string gherkinStep) { }
}

// LocatorStrategy.cs
public class LocatorStrategy
{
    public static ILocator FindElementWithFallback(IPage page, ElementLocators locators) { }
}
```

---

### 10.4 JavaScript/TypeScript Implementation

**Technology Stack**:
- **Browser**: Playwright for Node.js
- **BDD**: Cucumber.js
- **Runner**: Cucumber
- **Reporting**: Allure Cucumber

**Key Classes**:
```typescript
// universalStepDefinition.ts
Given(/^(.*)$/, async function(stepText: string) {
    await jsonExecutor.executeStep(stepText);
});

When(/^(.*)$/, async function(stepText: string) {
    await jsonExecutor.executeStep(stepText);
});

Then(/^(.*)$/, async function(stepText: string) {
    await jsonExecutor.executeStep(stepText);
});

// stepRepository.ts
export class StepRepository {
    static stepExists(gherkinStep: string): boolean { }
    static getStep(gherkinStep: string): StepData | null { }
}

// locatorStrategy.ts
export class LocatorStrategy {
    static async findElementWithFallback(
        page: Page, 
        locators: ElementLocators
    ): Promise<Locator | null> { }
}
```

---

## 11. Critical Implementation Rules

### 11.1 Never Assume - All Rules are Mandatory

1. **Every language MUST implement ALL 17 attributes**
2. **Missing attributes = `null`, NEVER empty string `""`**
3. **Step filename normalization must be identical**
4. **Locator priority order must not change**
5. **Fingerprint structure must be exact**
6. **Self-healing order: Label → Semantic → Proximity → Fuzzy**
7. **Universal step definition MUST catch all steps**
8. **JSON repository MUST be first, fallback second**

---

### 11.2 Non-Negotiable Principles

| Principle | Rationale |
|-----------|-----------|
| **Single Entry Point** | All steps go through one handler |
| **JSON First, Code Second** | Enforce no-code approach |
| **17 Attributes Always** | Maximum resilience |
| **Fingerprint DNA Required** | Enable self-healing |
| **null vs empty string** | Consistency in data model |
| **Live Attribute Enrichment** | Always reflect current DOM state |
| **Handler Delegation** | Single Responsibility Principle |
| **Locator Priority** | Stability over flexibility |

---

### 11.3 Testing Before Production

**Validation Checklist**:
- [ ] Browser launches successfully
- [ ] JSON step executes correctly
- [ ] Traditional step fallback works
- [ ] Parameter extraction works for quoted values
- [ ] Date resolution works (TODAY, TOMORROW)
- [ ] URL resolution works
- [ ] Self-healing triggers when locator fails
- [ ] Healing report shows before/after
- [ ] Allure report generates with screenshots
- [ ] Video recording attaches to report
- [ ] Parallel execution runs without conflicts

---

## 12. Migration Example

### 12.1 Java to Python Migration

**Java Original**:
```java
@When("^(.*)$")
public void executeStep(String step) {
    String fileName = StepRepository.generateStepFileName(step);
    if (StepRepository.stepExists(fileName)) {
        jsonExecutor.executeStep(step);
    } else {
        // fallback
    }
}
```

**Python Equivalent**:
```python
@when(step_matcher="re", pattern=r"^(.*)$")
def execute_step(context, step_text):
    file_name = StepRepository.generate_step_file_name(step_text)
    if StepRepository.step_exists(file_name):
        json_executor.execute_step(step_text)
    else:
        # fallback
```

---

### 12.2 Locator Strategy Migration

**Java**:
```java
public static Locator findElementWithFallback(Page page, ElementLocators locators) {
    List<LocatorAttempt> attempts = buildLocatorAttempts(locators);
    for (LocatorAttempt attempt : attempts) {
        try {
            Locator loc = page.locator(attempt.selector);
            if (loc.count() > 0) {
                return loc;
            }
        } catch (Exception e) {
            continue;
        }
    }
    return null;
}
```

**Python**:
```python
@staticmethod
def find_element_with_fallback(page: Page, locators: ElementLocators) -> Optional[Locator]:
    attempts = LocatorStrategy.build_locator_attempts(locators)
    for attempt in attempts:
        try:
            loc = page.locator(attempt['selector'])
            if loc.count() > 0:
                return loc
        except Exception:
            continue
    return None
```

---

## 13. Final Checklist

### 13.1 Framework Components

- [ ] Universal Step Definition (catches all steps)
- [ ] JSON Enhanced Executor (orchestrates actions)
- [ ] Step Repository (CRUD for JSON files)
- [ ] Data Models (StepData, ActionData, ElementLocators)
- [ ] Smart Locator Finder (orchestrates resolution)
- [ ] Locator Strategy (priority fallback)
- [ ] Self-Healing Engine (fingerprint DNA)
- [ ] Navigation Handler
- [ ] Interaction Handler  
- [ ] Verification Handler
- [ ] Playwright Manager (browser lifecycle)
- [ ] Parameter Extractor (quoted values)
- [ ] Date Resolver (TODAY, TOMORROW, etc.)
- [ ] URL Resolver (environment-based)
- [ ] Error Reporter (Allure integration)
- [ ] Traditional Steps (fallback)

---

### 13.2 Configuration Files

- [ ] pom.xml (or equivalent build config)
- [ ] TestNG/Runner configuration
- [ ] Allure properties
- [ ] Environment configs (dev/staging/prod)
- [ ] .gitignore (exclude reports, target, node_modules)

---

### 13.3 JSON Schema

- [ ] 17 mandatory attributes defined
- [ ] Fingerprint structure (attributes + context)
- [ ] Action types enumerated
- [ ] Parameterization pattern (`___RUNTIME_PARAMETER___`)
- [ ] Metadata structure (createdDate, totalActions)

---

### 13.4 Documentation

- [ ] README with setup instructions
- [ ] Architecture diagram
- [ ] Class descriptions
- [ ] Execution flow diagrams
- [ ] Migration guide for other languages
- [ ] Example feature files
- [ ] Example JSON files

---

## 14. Appendix

### 14.1 Example Feature File

```gherkin
@sanity @login
Feature: User Authentication
  
  Scenario: Successful Login
    Given User navigates to "https://example.com/login"
    When User enters username "admin" and password "Test@123"
    And User clicks login button
    Then User should see "Welcome, Admin"
    And User should see dashboard
```

---

### 14.2 Example JSON Repository Files

**File**: `user_navigates_to_param.json`
```json
{
  "stepFileName": "user_navigates_to_param",
  "gherkinStep": "User navigates to \"https://example.com\"",
  "normalizedStep": "user_navigates_to_param",
  "stepType": "Given",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "NAVIGATE",
      "description": "Navigate to URL",
      "value": "___RUNTIME_PARAMETER___"
    }
  ]
}
```

**File**: `user_clicks_login_button.json`
```json
{
  "stepFileName": "user_clicks_login_button",
  "gherkinStep": "User clicks login button",
  "actions": [
    {
      "actionNumber": 1,
      "actionType": "CLICK",
      "description": "Click Login Button",
      "element": {
        "type": "button",
        "id": "login-btn",
        "selector": "#login-btn",
        "text": "Login",
        "role": "button",
        "fingerprint": {
          "attributes": {"type": "submit", "role": "button"},
          "context": {"nearbyText": "Forgot Password?", "parentTag": "form"}
        }
      }
    }
  ]
}
```

---

### 14.3 Glossary

| Term | Definition |
|------|------------|
| **Strong JSON** | JSON with all 17 attributes captured from live DOM |
| **Fingerprint DNA** | Contextual metadata for self-healing |
| **Universal Step** | Single regex that catches all Gherkin steps |
| **Locator Fallback** | Trying multiple locators in priority order |
| **Self-Healing** | Auto-recovery when locators break |
| **Handler Pattern** | Delegating actions to specialized classes |
| **Parameter Extraction** | Getting quoted values from Gherkin |
| **Live Enrichment** | Capturing attributes during execution |

---

### 14.4 References

- **Playwright Documentation**: https://playwright.dev
- **Cucumber Documentation**: https://cucumber.io
- **Allure Framework**: https://docs.qameta.io/allure
- **TestNG**: https://testng.org
- **Maven**: https://maven.apache.org

---

## END OF FRAMEWORK RECREATION GUIDE

**Status**: Complete, Comprehensive, Language-Agnostic  
**Version**: 1.0  
**Date**: 2026-01-15  
**Purpose**: Enable recreation of this exact framework in any programming language
