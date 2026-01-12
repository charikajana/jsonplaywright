package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.data.ActionData;
import com.framework.data.ElementLocators;
import com.framework.data.StepData;
import com.framework.data.StepRepository;
import com.framework.playwright.PlaywrightManager;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Live Auto-Recorder Tool (Robust Version)
 * 
 * Extracts element DNA directly in the browser to avoid race conditions
 * during page navigation. Captures Clicks, Typing, and multi-tab flows.
 */
public class AutoRecorder {
    
    private static final StepRepository repository = new StepRepository();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static int stepCounter = 1;
    
    public static void main(String[] args) {
        String startUrl = (args.length > 0) ? args[0] : "https://www.google.com";
        
        System.out.println("═══ 🚀 StrongJSON Live Recorder 🚀 ═══\n");
        System.out.println("🌐 Target: " + startUrl);
        System.out.println("⚡ Instructions:");
        System.out.println("  1. Interact with the browser (Click/Type)");
        System.out.println("  2. Watch this console for generated steps");
        System.out.println("  3. Type 'exit' and press Enter to finish\n");

        try {
            PlaywrightManager manager = PlaywrightManager.getInstance();
            manager.initializeBrowser();
            Page page = manager.getPage();
            
            // 1. Setup multi-tab recording
            com.microsoft.playwright.BrowserContext context = manager.getContext();
            context.onPage(newPage -> {
                System.out.println("🆕 New tab detected: " + newPage.url());
                setupRecordingForPage(newPage);
            });

            // 2. Setup initial page
            setupRecordingForPage(page);

            // 3. Navigate
            page.navigate(startUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            System.out.println("✅ Recording active! Multi-tab support enabled.\n");

            // Keep alive
            Scanner scanner = new Scanner(System.in);
            System.out.println("TYPE 'exit' AND PRESS ENTER TO FINISH:");
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().equalsIgnoreCase("exit")) break;
            }
            
            manager.closeBrowser();
            System.out.println("\n👋 Recording finished. Check src/test/resources/locatorRepository/");

        } catch (Exception e) {
            System.err.println("❌ Recorder Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupRecordingForPage(Page page) {
        // Expose function to receive complete element data
        page.exposeFunction("recordInteractionNative", (Object[] args) -> {
            try {
                String json = (String) args[0];
                JsonNode data = mapper.readTree(json);
                handleNativeInteraction(data);
            } catch (Exception e) {
                System.err.println("Error parsing interaction: " + e.getMessage());
            }
            return null;
        });

        // Inject robust script to extract DNA at event time
        page.addInitScript("""
            const getXPath = (el) => {
                // 1. Direct ID is best
                if (el.id) return `//*[@id="${el.id}"]`;
                
                // 2. Name is second best
                if (el.name) return `//${el.tagName.toLowerCase()}[@name="${el.name}"]`;
                
                // 3. Text content (for links/buttons)
                if (el.tagName === 'A' || el.tagName === 'BUTTON' || (el.tagName === 'SPAN' && el.innerText.length < 30)) {
                    const text = (el.innerText || el.textContent || '').trim();
                    if (text) return `//${el.tagName.toLowerCase()}[contains(text(), "${text}")]`;
                }

                // 4. Relative to closest parent with ID
                let path = '';
                let current = el;
                while (current && current.nodeType === Node.ELEMENT_NODE) {
                    if (current !== el && current.id) {
                        path = `//*[@id="${current.id}"]//` + path;
                        return path.replace(/\\/\\/$/, '');
                    }
                    
                    let index = 0;
                    let sibling = current.previousSibling;
                    while (sibling) {
                        if (sibling.nodeType === Node.ELEMENT_NODE && sibling.tagName === current.tagName) index++;
                        sibling = sibling.previousSibling;
                    }
                    const seg = current.tagName.toLowerCase() + (index > 0 ? `[${index + 1}]` : '');
                    path = seg + (path ? '/' + path : '');
                    current = current.parentElement;
                }
                
                return '//' + path;
            };

            const extractDNA = (el, type, value) => {
                // Find a human-readable label for the element
                let humanLabel = null;
                
                // 1. Check for associated <label> element
                if (el.id) {
                    const label = document.querySelector(`label[for="${el.id}"]`);
                    if (label) humanLabel = label.innerText.replace(/[*:]/g, '').trim();
                }
                
                // 2. Check for parent <label> wrapper
                if (!humanLabel) {
                    const parentLabel = el.closest('label');
                    if (parentLabel) humanLabel = parentLabel.innerText.replace(/[*:]/g, '').trim();
                }
                
                // 3. Check for nearby text (prev sibling or parent's first child)
                if (!humanLabel && (el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA')) {
                    const prev = el.previousElementSibling;
                    if (prev && prev.innerText) {
                        humanLabel = prev.innerText.replace(/[*:]/g, '').trim();
                    } else if (el.parentElement.innerText) {
                        // Take first 20 chars of parent text if no direct label
                        const pText = el.parentElement.innerText.split('\\n')[0].trim();
                        if (pText && pText.length < 50) humanLabel = pText.replace(/[*:]/g, '').trim();
                    }
                }

                return JSON.stringify({
                    type: type,
                    value: value,
                    text: el.innerText || el.textContent || '',
                    humanLabel: humanLabel,
                    attributes: {
                        id: el.id || null,
                        name: el.name || el.getAttribute('name') || null,
                        className: el.className || null,
                        tagName: el.tagName.toLowerCase(),
                        typeAttr: el.getAttribute('type') || null,
                        placeholder: el.getAttribute('placeholder') || null,
                        role: el.getAttribute('role') || null,
                        ariaLabel: el.getAttribute('aria-label') || null,
                        dataTest: el.getAttribute('data-testid') || null,
                        href: el.getAttribute('href') || null,
                        title: el.getAttribute('title') || null
                    },
                    xpath: getXPath(el)
                });
            };

            // Capture Clicks & State Changes (Checkbox/Radio)
            window.addEventListener('mousedown', (e) => {
                const el = e.target;
                
                // IGNORE clicks on text inputs, textareas, and selects
                // These will be captured by the 'change' event instead
                const isTextinput = (el.tagName === 'INPUT' && !['checkbox', 'radio', 'button', 'submit'].includes(el.type));
                if (isTextinput || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT') {
                    return;
                }

                let type = 'CLICK';
                let value = '';
                
                if (el.tagName === 'INPUT' && el.type === 'checkbox') {
                    type = el.checked ? 'UNCHECK' : 'CHECK';
                } else if (el.tagName === 'INPUT' && el.type === 'radio') {
                    type = 'RADIO';
                }
                
                const dna = extractDNA(el, type, value);
                window.recordInteractionNative(dna);
            }, true);

            // Capture Hover (Mouse Over)
            let hoverTimeout;
            window.addEventListener('mouseover', (e) => {
                const el = e.target;
                // Ignore hover on input fields as they are usually technical noise
                if (['INPUT', 'TEXTAREA', 'SELECT'].includes(el.tagName)) return;
                
                if (['BUTTON', 'A', 'IMG', 'I', 'SPAN'].includes(el.tagName)) {
                    clearTimeout(hoverTimeout);
                    hoverTimeout = setTimeout(() => {
                        const dna = extractDNA(el, 'HOVER', '');
                        window.recordInteractionNative(dna);
                    }, 800); // 800ms dwell time to count as an intentional hover
                }
            }, true);

            // Capture Typing and Select Dropdowns
            window.addEventListener('change', (e) => {
                const el = e.target;
                if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                    const dna = extractDNA(el, 'TYPE', el.value);
                    window.recordInteractionNative(dna);
                } else if (el.tagName === 'SELECT') {
                    const selectedOption = el.options[el.selectedIndex].text;
                    const dna = extractDNA(el, 'SELECT', selectedOption);
                    window.recordInteractionNative(dna);
                }
            }, true);
        """);
    }

    private static void handleNativeInteraction(JsonNode data) {
        try {
            String type = data.get("type").asText();
            String value = data.get("value").asText();
            String text = data.get("text").asText();
            JsonNode attrs = data.get("attributes");
            String xpath = data.get("xpath").asText();

            // 1. Build ElementLocators
            ElementLocators locators = new ElementLocators();
            locators.setId(attrs.get("id").asText(null));
            locators.setName(attrs.get("name").asText(null));
            locators.setClassName(attrs.get("className").asText(null));
            locators.setType(attrs.get("typeAttr").asText(attrs.get("tagName").asText()));
            locators.setText(text.isEmpty() ? null : text.trim());
            locators.setPlaceholder(attrs.get("placeholder").asText(null));
            locators.setValue(value.isEmpty() ? null : value);
            locators.setHref(attrs.get("href").asText(null));
            locators.setRole(attrs.get("role").asText(null));
            locators.setAriaLabel(attrs.get("ariaLabel").asText(null));
            locators.setDataTest(attrs.get("dataTest").asText(null));
            locators.setTitle(attrs.get("title").asText(null));
            locators.setXpath(xpath);
            locators.setSelector(xpath);

            // Fingerprint
            ElementLocators.FingerprintData fingerprint = new ElementLocators.FingerprintData();
            ElementLocators.Attributes fAttrs = new ElementLocators.Attributes();
            fAttrs.setType(locators.getType());
            fAttrs.setAriaLabel(locators.getAriaLabel());
            fAttrs.setClassList(locators.getClassName());
            fAttrs.setRole(locators.getRole());
            fingerprint.setAttributes(fAttrs);
            locators.setFingerprint(fingerprint);

            // 2. Determine Gherkin Step Name
            String targetLabel = "";
            String humanLabel = data.has("humanLabel") ? data.get("humanLabel").asText(null) : null;

            if (humanLabel != null && !humanLabel.isEmpty()) {
                targetLabel = humanLabel;
            } else if (locators.getPlaceholder() != null) {
                targetLabel = locators.getPlaceholder();
            } else if (locators.getAriaLabel() != null) {
                targetLabel = locators.getAriaLabel();
            } else if (!text.trim().isEmpty()) {
                targetLabel = text.trim();
            } else {
                // Last resort: Clean the ID if possible
                String rawId = locators.getId();
                if (rawId != null) {
                    // Remove common .NET/Java prefixes like ctl00, cph_ etc
                    targetLabel = rawId.replaceAll("(?i)ctl[0-9]+|cph|maincontent|uc|lbl|_", " ").trim();
                    if (targetLabel.isEmpty()) targetLabel = rawId;
                } else if (locators.getName() != null) {
                    targetLabel = locators.getName();
                } else {
                    targetLabel = "element_" + stepCounter;
                }
            }

            // Add descriptive suffix based on element type
            String tagName = attrs.get("tagName").asText().toLowerCase();
            String typeAttr = attrs.get("typeAttr") != null ? attrs.get("typeAttr").asText().toLowerCase() : "";
            String suffix = "";

            if (tagName.equals("input")) {
                if (typeAttr.equals("submit") || typeAttr.equals("button")) suffix = " Button";
                else if (typeAttr.equals("checkbox")) suffix = " Checkbox";
                else if (typeAttr.equals("radio")) suffix = " Radio Button";
                else suffix = " Input Box";
            } else if (tagName.equals("button")) {
                suffix = " Button";
            } else if (tagName.equals("a")) {
                suffix = " Link";
            } else if (tagName.equals("select")) {
                suffix = " Dropdown";
            } else if (tagName.equals("textarea")) {
                suffix = " Text Area";
            }

            if (targetLabel.length() > 40) targetLabel = targetLabel.substring(0, 37) + "...";
            
            String verb = "";
            boolean isParameterized = false;
            
            if (type.equals("CLICK")) {
                if (tagName.equals("li") || (tagName.equals("div") && xpath.contains("/li"))) {
                    verb = "selects suggestion ";
                    suffix = "";
                } else {
                    verb = "clicks on ";
                }
            } else if (type.equals("CHECK")) {
                verb = "checks ";
            } else if (type.equals("UNCHECK")) {
                verb = "unchecks ";
            } else if (type.equals("RADIO")) {
                verb = "selects ";
            } else if (type.equals("HOVER")) {
                verb = "hovers over ";
            } else if (type.equals("TYPE")) {
                verb = "enters \"" + value + "\" into ";
                isParameterized = true;
            } else if (type.equals("SELECT")) {
                verb = "selects \"" + value + "\" from ";
                isParameterized = true;
            }

            String gherkin = "When User " + verb + targetLabel + suffix;

            // 3. Create and Save Step
            StepData step = new StepData();
            step.setGherkinStep(gherkin);
            step.setStepNumber(stepCounter++);
            
            ActionData action = new ActionData();
            action.setActionNumber(1);
            action.setActionType(type);
            action.setElement(locators);
            
            // For TYPE and SELECT, use runtime parameter marker
            if (isParameterized) {
                action.setValue("___RUNTIME_PARAMETER___");
            } else {
                action.setValue(null);
            }
            
            List<ActionData> actionList = new ArrayList<>();
            actionList.add(action);
            step.setActions(actionList);
            
            repository.saveStep(step);
            
            System.out.println("✨ Generated: " + gherkin);
            System.out.println("   📁 Saved to: src/test/resources/locatorRepository/\n");

        } catch (Exception e) {
            System.err.println("Error processing interaction: " + e.getMessage());
        }
    }
}
