package com.framework.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.framework.data.StepData;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Step Repository - One JSON file per unique step
 * Automatically reuses existing steps and creates new ones when needed
 */
public class StepRepository {
    private static final Logger logger = LoggerFactory.getLogger(StepRepository.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String STEP_REPOSITORY_DIR = "src/test/resources/locatorRepository/";
    
    /**
     * Check if a step already exists in the repository
     * @param gherkinStep The Gherkin step text
     * @return true if step exists, false otherwise
     */
    public static boolean stepExists(String gherkinStep) {
        String stepFileName = generateStepFileName(gherkinStep);
        File stepFile = new File(STEP_REPOSITORY_DIR + stepFileName + ".json");
        return stepFile.exists();
    }
    
    /**
     * Get step data from repository
     * @param gherkinStep The Gherkin step text
     * @return StepData if found, null otherwise
     */
    public static StepData getStep(String gherkinStep) {
        String stepFileName = generateStepFileName(gherkinStep);
        File stepFile = new File(STEP_REPOSITORY_DIR + stepFileName + ".json");
        
        if (!stepFile.exists()) {
            logger.debug("Step not found in repository: {}", gherkinStep);
            return null;
        }
        
        try {
            JsonNode stepNode = objectMapper.readTree(stepFile);
            StepData stepData = parseStepData(stepNode);
            logger.info("[OK] Loaded step from repository: {}", gherkinStep);
            return stepData;
            
        } catch (IOException e) {
            logger.error("Error reading step from repository: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Save a step to the repository
     * @param stepData The step data to save
     * @return true if saved successfully, false otherwise
     */
    public static boolean saveStep(StepData stepData) {
        if (stepData == null || stepData.getGherkinStep() == null) {
            logger.warn("Cannot save null step data");
            return false;
        }
        
        String stepFileName = generateStepFileName(stepData.getGherkinStep());
        File stepFile = new File(STEP_REPOSITORY_DIR + stepFileName + ".json");
        
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(STEP_REPOSITORY_DIR));
            
            // Convert StepData to JSON
            ObjectNode stepNode = objectMapper.createObjectNode();
            stepNode.put("stepFileName", stepFileName);
            stepNode.put("gherkinStep", stepData.getGherkinStep());
            stepNode.put("normalizedStep", normalizeStepText(stepData.getGherkinStep()));
            stepNode.put("stepType", stepData.getStepType());
            stepNode.put("stepNumber", stepData.getStepNumber());
            stepNode.put("status", stepData.getStatus());
            
            // Add actions array
            stepNode.set("actions", objectMapper.valueToTree(stepData.getActions()));
            
            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("createdDate", java.time.LocalDateTime.now().toString());
            metadata.put("totalActions", stepData.getActions().size());
            stepNode.set("metadata", metadata);
            
            // Save to file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(stepFile, stepNode);
            
            logger.info("[OK] Saved step to repository: {} -> {}", stepData.getGherkinStep(), stepFile.getName());
            return true;
            
        } catch (IOException e) {
            logger.error("Error saving step to repository: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get element locators from a step in the repository
     * @param gherkinStep The Gherkin step text
     * @return ElementLocators if found, null otherwise
     */
    public static ElementLocators getLocators(String gherkinStep) {
        StepData stepData = getStep(gherkinStep);
        if (stepData == null) {
            return null;
        }
        
        // Extract locators from first action with an element
        for (ActionData action : stepData.getActions()) {
            if (action.getElement() != null) {
                return action.getElement();
            }
        }
        return null;
    }
    
    /**
     * Get actions from a step in the repository
     * @param gherkinStep The Gherkin step text
     * @return List of ActionData, empty list if not found
     */
    public static List<ActionData> getActions(String gherkinStep) {
        StepData stepData = getStep(gherkinStep);
        if (stepData == null) {
            return new ArrayList<>();
        }
        return stepData.getActions();
    }
    
    /**
     * Update an existing step or create if it doesn't exist
     * @param stepData The step data to save/update
     * @return true if successful, false otherwise
     */
    public static boolean saveOrUpdateStep(StepData stepData) {
        if (stepExists(stepData.getGherkinStep())) {
            logger.info("Step already exists, updating: {}", stepData.getGherkinStep());
        }
        return saveStep(stepData);
    }
    
    /**
     * Get the file path for a step (useful for debugging)
     * @param gherkinStep The Gherkin step text
     * @return File path as string
     */
    public static String getStepFilePath(String gherkinStep) {
        String stepFileName = generateStepFileName(gherkinStep);
        return STEP_REPOSITORY_DIR + stepFileName + ".json";
    }
    
    /**
     * Delete a step from the repository
     * @param gherkinStep The Gherkin step text
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteStep(String gherkinStep) {
        String stepFileName = generateStepFileName(gherkinStep);
        File stepFile = new File(STEP_REPOSITORY_DIR + stepFileName + ".json");
        
        if (stepFile.exists()) {
            boolean deleted = stepFile.delete();
            if (deleted) {
                logger.info("[OK] Deleted step from repository: {}", gherkinStep);
            }
            return deleted;
        }
        return false;
    }
    
    /**
     * List all steps in the repository
     * @return List of all step file names
     */
    public static List<String> listAllSteps() {
        List<String> steps = new ArrayList<>();
        File dir = new File(STEP_REPOSITORY_DIR);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return steps;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    JsonNode stepNode = objectMapper.readTree(file);
                    String gherkinStep = stepNode.get("gherkinStep").asText();
                    steps.add(gherkinStep);
                } catch (IOException e) {
                    logger.warn("Error reading step file: {}", file.getName());
                }
            }
        }
        
        return steps;
    }
    
    /**
     * Generate a human-readable filename for a step based on its text
     * Converts step text to a safe, readable filename
     * @param gherkinStep The Gherkin step text
     * @return Sanitized filename (without .json extension)
     */
    private static String generateStepFileName(String gherkinStep) {
        String normalized = normalizeStepText(gherkinStep);
        
        // Sanitize for filename:
        // 1. Replace spaces with underscores
        // 2. Remove special characters except underscores and hyphens
        // 3. Limit length to avoid filesystem issues
        // 4. Convert to lowercase
        
        String sanitized = normalized
            .replaceAll("['\"`]", "")           // Remove quotes
            .replaceAll("[^a-z0-9\\s_-]", "")    // Keep alphanumeric, spaces, underscores, hyphens
            .trim()
            .replaceAll("\\s+", "_")            // Replace spaces with underscores
            .replaceAll("_+", "_")              // Remove duplicate underscores
            .replaceAll("^_|_$", "");           // Remove leading/trailing underscores
        
        // Limit length to 100 characters for filesystem compatibility
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
            // Remove trailing underscore if truncation created one
            sanitized = sanitized.replaceAll("_$", "");
        }
        
        // Ensure filename is not empty
        if (sanitized.isEmpty()) {
            sanitized = "step_" + Math.abs(gherkinStep.hashCode());
        }
        
        return sanitized;
    }
    
    /**
     * Normalize step text for comparison and hashing
     * Handles:
     * - Quoted parameters: "value"
     * - Angle-bracket parameters: <param>
     * - Standalone numbers (from Scenario Outline substitution): 25 -> _param_
     */
    private static String normalizeStepText(String stepText) {
        return stepText
            .replaceAll("^(Given|When|Then|And|But)\\s+", "")
            .replaceAll("\"[^\"]*\"", "_param_")     // Mask quoted values: "value"
            .replaceAll("<[^>]+>", "_param_")        // Mask angle-bracket params: <param>
            .replaceAll("\\b\\d+\\b", "_param_")     // Mask standalone numbers: 25
            .replaceAll("(_param_\\s*)+", "_param_ ") // Collapse consecutive params
            .trim()
            .toLowerCase();
    }
    
    /**
     * Parse step data from JSON node
     */
    private static StepData parseStepData(JsonNode stepNode) {
        StepData step = new StepData();
        step.setGherkinStep(stepNode.path("gherkinStep").asText(""));
        step.setStepType(stepNode.path("stepType").asText(null));
        step.setStepNumber(stepNode.path("stepNumber").asInt(0));
        step.setStatus(stepNode.path("status").asText(null));
        
        List<ActionData> actions = new ArrayList<>();
        JsonNode actionsNode = stepNode.get("actions");
        
        if (actionsNode != null && actionsNode.isArray()) {
            for (JsonNode actionNode : actionsNode) {
                ActionData action = parseActionData(actionNode);
                actions.add(action);
            }
        }
        
        step.setActions(actions);
        return step;
    }
    
    /**
     * Parse action data from JSON node
     */
    private static ActionData parseActionData(JsonNode actionNode) {
        try {
            return objectMapper.treeToValue(actionNode, ActionData.class);
        } catch (IOException e) {
            logger.error("Error deserializing ActionData: {}", e.getMessage());
            // Fallback to minimal manual parsing if treeToValue fails
            ActionData action = new ActionData();
            action.setActionType(actionNode.get("actionType").asText());
            action.setDescription(actionNode.get("description").asText());
            return action;
        }
    }
    
    /**
     * Parse element locators from JSON node
     */
    private static ElementLocators parseElementLocators(JsonNode elementNode) {
        try {
            return objectMapper.treeToValue(elementNode, ElementLocators.class);
        } catch (IOException e) {
            logger.error("Error deserializing ElementLocators: {}", e.getMessage());
            return new ElementLocators();
        }
    }

    /**
     * Populates live attributes from a Playwright locator into the locators model.
     * This makes the JSON "strong" by capturing every possible identity signal.
     */
    public static void populateLiveAttributes(Locator element, ElementLocators locators) {
        if (element == null || locators == null) return;
        try {
            // Use JavaScript to get both explicit attributes and computed properties
            // Includes a relative XPath generator to ensure the "xpath" field is not null
            String jsonAttributes = (String) element.evaluate("el => {" +
                "  const getXPath = (element) => {" +
                "    if (element.id) return `//${element.tagName.toLowerCase()}[@id='${element.id}']`;" +
                "    if (element.name) return `//${element.tagName.toLowerCase()}[@name='${element.name}']`;" +
                "    const paths = [];" +
                "    for (; element && element.nodeType === 1; element = element.parentNode) {" +
                "      let index = 0;" +
                "      for (let sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {" +
                "        if (sibling.nodeType === 1 && sibling.nodeName === element.nodeName) index++;" +
                "      }" +
                "      const tagName = element.nodeName.toLowerCase();" +
                "      const pathIndex = (index ? `[${index + 1}]` : '');" +
                "      paths.unshift(tagName + pathIndex);" +
                "      if (element.id) break;" +
                "    }" +
                "    let xpath = paths.join('/');" +
                "    return xpath.startsWith('html') ? '/' + xpath : '//' + xpath;" +
                "  };" +
                "  " +
                "  return JSON.stringify({" +
                "    id: el.id || el.getAttribute('id') || null," +
                "    name: el.name || el.getAttribute('name') || null," +
                "    type: el.type || el.getAttribute('type') || null," +
                "    placeholder: el.placeholder || el.getAttribute('placeholder') || null," +
                "    title: el.title || el.getAttribute('title') || null," +
                "    alt: el.alt || el.getAttribute('alt') || null," +
                "    className: el.className || el.getAttribute('class') || null," +
                "    href: el.href || el.getAttribute('href') || null," +
                "    src: el.src || el.getAttribute('src') || null," +
                "    value: el.value || el.getAttribute('value') || null," +
                "    ariaLabel: el.getAttribute('aria-label') || null," +
                "    role: el.getAttribute('role') || null," +
                "    xpath: getXPath(el)" +
                "  });" +
                "}");
            
            JsonNode attrNode = objectMapper.readTree(jsonAttributes);
            
            // Revert empty strings to NULL if they are missing on page
            locators.setId(attrNode.path("id").isMissingNode() || attrNode.path("id").isNull() ? null : attrNode.path("id").asText());
            locators.setName(attrNode.path("name").isMissingNode() || attrNode.path("name").isNull() ? null : attrNode.path("name").asText());
            locators.setType(attrNode.path("type").asText(locators.getType()));
            locators.setRole(attrNode.path("role").isMissingNode() || attrNode.path("role").isNull() ? null : attrNode.path("role").asText());
            locators.setAriaLabel(attrNode.path("ariaLabel").isMissingNode() || attrNode.path("ariaLabel").isNull() ? null : attrNode.path("ariaLabel").asText());
            locators.setPlaceholder(attrNode.path("placeholder").isMissingNode() || attrNode.path("placeholder").isNull() ? null : attrNode.path("placeholder").asText());
            locators.setTitle(attrNode.path("title").isMissingNode() || attrNode.path("title").isNull() ? null : attrNode.path("title").asText());
            locators.setAlt(attrNode.path("alt").isMissingNode() || attrNode.path("alt").isNull() ? null : attrNode.path("alt").asText());
            locators.setClassName(attrNode.path("className").isMissingNode() || attrNode.path("className").isNull() ? null : attrNode.path("className").asText());
            locators.setValue(attrNode.path("value").isMissingNode() || attrNode.path("value").isNull() ? null : attrNode.path("value").asText());
            locators.setHref(attrNode.path("href").isMissingNode() || attrNode.path("href").isNull() ? null : attrNode.path("href").asText());
            locators.setSrc(attrNode.path("src").isMissingNode() || attrNode.path("src").isNull() ? null : attrNode.path("src").asText());
            locators.setXpath(attrNode.path("xpath").isMissingNode() || attrNode.path("xpath").isNull() ? null : attrNode.path("xpath").asText());
            
            // Refresh text
            String liveText = element.innerText();
            locators.setText(liveText != null && !liveText.trim().isEmpty() ? liveText.trim() : null);
            
        } catch (Exception e) {
            logger.debug("[INFO] Could not fetch all live attributes: {}", e.getMessage());
        }
    }
}
