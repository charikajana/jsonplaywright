package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.data.StepData;
import com.framework.data.StepRepository;
import com.framework.playwright.PlaywrightManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Chrome DevTools Recorder to StrongJSON Converter
 * 
 * Converts Chrome Recorder JSON exports into your framework's
 * StrongJSON format with all 17 element attributes!
 */
public class ChromeRecorderConverter {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final StepRepository repository = new StepRepository();
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("═══ 🔄 Chrome Recorder Converter 🔄 ═══\n");
            System.out.println("Usage: mvn test-compile exec:java \\");
            System.out.println("  -Dexec.mainClass=\"examples.ChromeRecorderConverter\" \\");
            System.out.println("  -Dexec.classpathScope=test \\");
            System.out.println("  -Dexec.args=\"path/to/recording.json\"");
            System.out.println("\nExample:");
            System.out.println("  -Dexec.args=\"recordings/sabre-login.json\"\n");
            return;
        }
        
        String recordingFile = args[0];
        
        try {
            System.out.println("═══ 🔄 Chrome Recorder Converter 🔄 ═══\n");
            System.out.println("📂 Reading: " + recordingFile + "\n");
            
            // Read Chrome Recorder JSON
            JsonNode recording = mapper.readTree(new File(recordingFile));
            String title = recording.get("title").asText();
            JsonNode steps = recording.get("steps");
            
            System.out.println("📝 Recording: " + title);
            System.out.println("📊 Total steps: " + steps.size() + "\n");
            
            // Initialize browser to fetch element details
            System.out.println("🌐 Initializing browser...");
            PlaywrightManager manager = PlaywrightManager.getInstance();
            manager.initializeBrowser();
            Page page = manager.getPage();
            System.out.println("✅ Browser ready!\n");
            
            // Find navigation step
            String targetUrl = null;
            for (JsonNode step : steps) {
                if (step.get("type").asText().equals("navigate")) {
                    targetUrl = step.get("url").asText();
                    break;
                }
            }
            
            if (targetUrl != null) {
                System.out.println("🌐 Navigating to: " + targetUrl);
                page.navigate(targetUrl);
                page.waitForLoadState();
                System.out.println("✅ Page loaded!\n");
            }
            
            // Convert each action step
            List<StepData> generatedSteps = new ArrayList<>();
            int stepNum = 1;
            
            System.out.println("🔄 Converting steps...\n");
            
            for (JsonNode step : steps) {
                String type = step.get("type").asText();
                
                // Skip viewport and navigation
                if (type.equals("setViewport") || type.equals("navigate")) {
                    continue;
                }
                
                try {
                    StepData converted = convertStep(step, page, stepNum);
                    if (converted != null) {
                        generatedSteps.add(converted);
                        repository.saveStep(converted);
                        
                        // Generate simple filename
                        String fileName = converted.getGherkinStep().toLowerCase()
                            .replaceAll("[^a-z0-9]+", "_")
                            .replaceAll("^_|_$", "");
                        System.out.println("✅ Step " + stepNum + ": " + converted.getGherkinStep());
                        System.out.println("   Actions: " + converted.getActions().size());
                        System.out.println("   File: " + fileName + ".json\n");
                        
                        stepNum++;
                    }
                } catch (Exception e) {
                    System.err.println("⚠️  Failed to convert step (type: " + type + "): " + e.getMessage());
                }
            }
            
            // Summary
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    🎉 SUCCESS! 🎉                         ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("📊 Conversion Results:");
            System.out.println("  - Chrome Recorder steps: " + steps.size());
            System.out.println("  - StrongJSON files created: " + generatedSteps.size());
            System.out.println("  - Location: src/test/resources/locatorRepository/\n");
            
            System.out.println("📝 Generated Files:");
            for (StepData step : generatedSteps) {
                String fileName = step.getGherkinStep().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_|_$", "");
                System.out.println("  ✅ " + fileName + ".json");
            }
            
            System.out.println("\n💡 Next Steps:");
            System.out.println("  1. Review the generated JSON files");
            System.out.println("  2. Create a Feature file with these steps");
            System.out.println("  3. Run your tests!\n");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static StepData convertStep(JsonNode step, Page page, int stepNum) {
        String type = step.get("type").asText();
        
        // Get selectors
        JsonNode selectorsNode = step.get("selectors");
        if (selectorsNode == null || selectorsNode.size() == 0) {
            return null;
        }
        
        String selector = selectorsNode.get(0).get(0).asText();
        
        // Find element on page
        Locator element = page.locator(selector);
        if (element.count() == 0) {
            System.out.println("⚠️  Element not found: " + selector);
            return null;
        }
        
        // Get element details
        ElementLocators locators = extractElementDetails(element, selector);
        
        // Create action based on type
        ActionData action = new ActionData();
        action.setActionNumber(1);
        action.setElement(locators);
        
        String gherkinStep = "";
        
        switch (type) {
            case "click":
                action.setActionType("CLICK");
                gherkinStep = generateGherkinForClick(locators, stepNum);
                break;
                
            case "change":
                action.setActionType("TYPE");
                String value = step.get("value").asText();
                action.setValue(value);
                gherkinStep = generateGherkinForType(locators, stepNum);
                break;
                
            case "select":
                action.setActionType("SELECT");
                gherkinStep = generateGherkinForSelect(locators, stepNum);
                break;
                
            default:
                return null;
        }
        
        // Create StepData
        StepData stepData = new StepData();
        stepData.setGherkinStep(gherkinStep);
        stepData.setStepNumber(stepNum);
        List<ActionData> actions = new ArrayList<>();
        actions.add(action);
        stepData.setActions(actions);
        
        return stepData;
    }
    
    private static ElementLocators extractElementDetails(Locator element, String originalSelector) {
        ElementLocators locators = new ElementLocators();
        
        try {
            // Get all attributes
            locators.setId((String) element.getAttribute("id"));
            locators.setName((String) element.getAttribute("name"));
            locators.setClassName((String) element.getAttribute("class"));
            // Get tag name first
            String tagName = element.evaluate("el => el.tagName").toString().toLowerCase();
            locators.setType(tagName);  // Start with tag name
            locators.setText((String) element.textContent());
            locators.setPlaceholder((String) element.getAttribute("placeholder"));
            // For input elements, type attribute (text/password/etc) is more specific
            String typeAttr = (String) element.getAttribute("type");
            if (typeAttr != null && !typeAttr.isEmpty()) {
                locators.setType(typeAttr);  // Override with specific type
            }
            locators.setValue((String) element.getAttribute("value"));
            locators.setHref((String) element.getAttribute("href"));
            locators.setAlt((String) element.getAttribute("alt"));
            locators.setTitle((String) element.getAttribute("title"));
            locators.setRole((String) element.getAttribute("role"));
            locators.setAriaLabel((String) element.getAttribute("aria-label"));
            locators.setDataTest((String) element.getAttribute("data-testid"));
            locators.setSrc((String) element.getAttribute("src"));
            
            // Generate XPath
            String xpath = (String) element.evaluate("""
                el => {
                    if (el.id) return '//*[@id="' + el.id + '"]';
                    let path = '';
                    while (el && el.nodeType === Node.ELEMENT_NODE) {
                        let index = 0;
                        let sibling = el.previousSibling;
                        while (sibling) {
                            if (sibling.nodeType === Node.ELEMENT_NODE && sibling.tagName === el.tagName) index++;
                            sibling = sibling.previousSibling;
                        }
                        const tagName = el.tagName.toLowerCase();
                        path = '/' + tagName + (index > 0 ? '[' + (index + 1) + ']' : '') + path;
                        el = el.parentElement;
                    }
                    return path;
                }
                """);
            locators.setXpath(xpath);
            
            // CSS Selector (use original)
            locators.setCssSelector(originalSelector);
            locators.setSelector(originalSelector);
            
            // DNA Fingerprint (using FingerprintData structure)
            ElementLocators.FingerprintData fingerprint = new ElementLocators.FingerprintData();
            ElementLocators.Attributes attrs = new ElementLocators.Attributes();
            attrs.setType(locators.getType());
            attrs.setAriaLabel(locators.getAriaLabel());
            attrs.setClassList(locators.getClassName());
            attrs.setRole(locators.getRole());
            fingerprint.setAttributes(attrs);
            locators.setFingerprint(fingerprint);
            
        } catch (Exception e) {
            System.err.println("Error extracting element details: " + e.getMessage());
        }
        
        return locators;
    }
    
    private static String generateGherkinForClick(ElementLocators element, int stepNum) {
        String target = element.getText();
        if (target == null || target.isBlank()) {
            target = element.getPlaceholder();
        }
        if (target == null || target.isBlank()) {
            target = element.getName();
        }
        if (target == null || target.isBlank()) {
            target = element.getId();
        }
        if (target == null || target.isBlank()) {
            target = "element_" + stepNum;
        }
        
        return "When I click on " + target.trim();
    }
    
    private static String generateGherkinForType(ElementLocators element, int stepNum) {
        String field = element.getPlaceholder();
        if (field == null || field.isBlank()) {
            field = element.getName();
        }
        if (field == null || field.isBlank()) {
            field = element.getId();
        }
        if (field == null || field.isBlank()) {
            field = "field_" + stepNum;
        }
        
        return "When I enter text into " + field.trim();
    }
    
    private static String generateGherkinForSelect(ElementLocators element, int stepNum) {
        String dropdown = element.getName();
        if (dropdown == null || dropdown.isBlank()) {
            dropdown = element.getId();
        }
        if (dropdown == null || dropdown.isBlank()) {
            dropdown = "dropdown_" + stepNum;
        }
        
        return "When I select from " + dropdown.trim();
    }
}
