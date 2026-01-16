# 🎭 No-Code Playwright Automation Framework

A robust, hybrid test automation framework combining the power of **Playwright** and **Cucumber Gherkin**. 
This framework supports a unique **"No-Code"** approach using JSON locators while providing a seamless **fallback mechanism** to traditional Java step definitions.

---

## 🚀 Key Features

- **Hybrid Execution**: Automatically uses JSON-based locators. if not found, falls back to traditional Java steps.
- **Smart Element Selection**: Advanced locator strategy (ID > Name > CSS > XPath > Text > Placeholder).
- **Dynamic Reporting**: Automatically generates timestamped **Allure Reports** with attached screenshots and execution videos.
- **Parallel Support**: Thread-safe execution for high-speed testing across multiple threads.
- **Dynamic Data Generation**: Built-in support for generating and caching random guest details (names, emails, phones) with scenario-level persistence.
- **Agent Ready**: Designed to work with AI agents for visual automation and self-healing.

---

## 🎲 Dynamic Data Generation

The framework supports keyword-based random data generation. Keywords are resolved during execution and the values are **cached per scenario**, ensuring that if you use the same keyword twice in a test (e.g., type vs. verify), the value remains consistent.

### Supported Keywords:
| Keyword | Description | Example Result |
| :--- | :--- | :--- |
| `RANDOM_FIRST_NAME` | Generates a guest first name | `Guest1234` |
| `RANDOM_LAST_NAME` | Generates a user last name | `User789` |
| `RANDOM_EMAIL` | Generates a unique UUID-based email | `testuser_a1b2c3d4@example.com` |
| `RANDOM_PHONE` | Generates a 10-digit phone number | `9123456789` |
| `RANDOM_NUMERIC_X` | Generates X number of digits | `RANDOM_NUMERIC_5` -> `48291` |
| `RANDOM_ALPHABETIC_X`| Generates X number of letters | `RANDOM_ALPHABETIC_8` -> `aBcdEfGh` |

### Usage:
1. **In Gherkin**: `When User enters name "RANDOM_FIRST_NAME"`
2. **In JSON locators**: Set the `"value"` field to a keyword like `"RANDOM_EMAIL"` for automatic randomization.

---

## 📥 Capturing & Reusing Values (Variables)

This feature allows you to extract data from the UI (like a Booking ID) and use it in later steps.

### 1. Capture Logic (`GET_TEXT`)
To capture a value, use the `GET_TEXT` action type in your JSON repository. Specify a variable name (starting with `VAR_`) in the `"value"` field.

*   **Custom Naming**: You can use any name (e.g., `VAR_chari`, `VAR_testdata`). The framework converts these to uppercase internally for consistent lookup.
*   **Allure Visibility**: Captured variables are automatically attached to the Allure report, allowing you to Audit exactly what value was scraped from the UI.

```json
{
  "actionType": "GET_TEXT",
  "description": "Capture Booking Reference",
  "element": { "id": "conf-number" },
  "value": "VAR_BOOKING_ID"
}
```

### 2. Usage Logic (`VAR_`)
Anywhere you need to use that captured value (in `TYPE`, `SELECT`, `NAVIGATE`, or `VERIFY_TEXT`), simply use the variable name.

*   **In Gherkin:** `And User enters "VAR_BOOKING_ID" in search`
*   **In JSON:** `"value": "VAR_BOOKING_ID"`

### 3. Advanced Verification (`VERIFY_TEXT`)
The framework supports different matching strategies for verification. You can specify the `comparisonType` in your JSON action:

*   **CONTAINS** (Default): Checks if the actual UI text contains the expected value. Robust for partial matches.
*   **EXACTLY**: Performs a strict string equality check (trimmed).

**JSON Example:**
```json
{
  "actionType": "VERIFY_TEXT",
  "expectedText": "VAR_BOOKING_ID",
  "comparisonType": "EXACTLY",
  "element": { "css": ".ref-id" }
}
```

*In the report, both the **Expected** and **Actual** values are logged, making failures easy to debug.*

---

## 📋 Prerequisites

- **Java**: JDK 21 or higher.
- **Maven**: 3.6+ installed and configured in system PATH.
- **Browsers**: Framework will attempt to auto-install, but manual installation can be triggered.

---

## 🛠️ Setup and Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd jsonplaywright
   ```

2. **Install Playwright Browsers**:
   ```bash
   mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
   ```

---

## 🏃 Local Execution (Windows/Mac)

Execute tests using Maven with dynamic parameter support.

| Feature | Command |
| :--- | :--- |
| **All Sanity Tests** | `mvn test` |
| **Specific Tags** | `mvn test -DTags="@login"` |
| **Multiple Tags** | `mvn test -DTags="@login or @traditional"` |
| **Parallel Execution** | `mvn test -DThreadCount=4` |
| **Combine Both** | `mvn test -DTags="@sanity" -DThreadCount=2` |

---

## 🐧 Linux Execution (CI/Headless)

On Linux/CI environments, Playwright requires specific system dependencies.

1. **Install Linux Dependencies** (Ubuntu/Debian):
   ```bash
   mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install-deps"
   ```

2. **Execute Headless**:
   The framework is configured to run headless by default in Linux environments.
   ```bash
   mvn test -DBrowserName=chromium -DTags="@sanity"
   ```

---

## 📊 Reporting & Artifacts

All test artifacts are saved in a timestamped directory structure: `reports/DDMMM/HHMMSS/`

- **Allure Report**: Automatically generated and opened after the run.
- **Screenshots**: Saved for verification steps and automated on failure.
- **Videos**: Full execution videos attached to the Allure report for every scenario.

**Manual Allure Command**:
If you need to view results from a past run:
```bash
allure serve allure-results
```

---

## 📂 Project Structure

```text
src/
├── main/java/
│   ├── com.framework.playwright/  # Browser & Action wrappers
│   ├── com.framework.executor/    # Core No-Code logic
│   ├── com.framework.steps/       # Traditional Step Definitions
│   └── com.framework.utils/       # Reporting & Discovery utilities
├── test/java/
│   └── runner/                    # TestNG Runner
└── test/resources/
     ├── features/                 # Gherkin Feature files
     └── locatorRepository/        # JSON Step definitions
```

---

## 💡 Fallback Mechanism

1. Framework looks for a JSON file in `locatorRepository` matching the Gherkin step.
2. If **JSON is not found**, it automatically scans `com.framework.steps` for a Java method annotated with `@Given/When/Then` matching the text.
3. This allows you to mix "No-Code" ease for standard elements with "Traditional Code" for complex logic.

---

## 🛡️ License
Distributed under the MIT License.
