# 🎭 No-Code Playwright Automation Framework

A robust, hybrid test automation framework combining the power of **Playwright** and **Cucumber Gherkin**. 
This framework supports a unique **"No-Code"** approach using JSON locators while providing a seamless **fallback mechanism** to traditional Java step definitions.

---

## 🚀 Key Features

- **Hybrid Execution**: Automatically uses JSON-based locators. if not found, falls back to traditional Java steps.
- **Smart Element Selection**: Advanced locator strategy (ID > Name > CSS > XPath > Text > Placeholder).
- **Dynamic Reporting**: Automatically generates timestamped **Allure Reports** with attached screenshots and execution videos.
- **Parallel Support**: Thread-safe execution for high-speed testing across multiple threads.
- **Agent Ready**: Designed to work with AI agents for visual automation and self-healing.

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
