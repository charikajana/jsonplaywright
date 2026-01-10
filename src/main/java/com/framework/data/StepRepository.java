package com.framework.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            .replaceAll("[^a-z0-9\\s-]", "")     // Keep only alphanumeric, spaces, hyphens
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
     */
    private static String normalizeStepText(String stepText) {
        return stepText
            .replaceAll("^(Given|When|Then|And|But)\\s+", "")
            .replaceAll("\"[^\"]*\"", "___param___") // Mask quoted values directly
            .trim()
            .toLowerCase();
    }
    
    /**
     * Parse step data from JSON node
     */
    private static StepData parseStepData(JsonNode stepNode) {
        StepData step = new StepData();
        step.setGherkinStep(stepNode.get("gherkinStep").asText());
        step.setStepType(stepNode.get("stepType").asText());
        step.setStepNumber(stepNode.get("stepNumber").asInt());
        step.setStatus(stepNode.get("status").asText());
        
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
        ActionData action = new ActionData();
        action.setActionNumber(actionNode.get("actionNumber").asInt());
        action.setActionType(actionNode.get("actionType").asText());
        action.setDescription(actionNode.get("description").asText());
        
        if (actionNode.has("element")) {
            JsonNode elementNode = actionNode.get("element");
            ElementLocators locators = parseElementLocators(elementNode);
            action.setElement(locators);
        }
        
        if (actionNode.has("value")) {
            action.setValue(actionNode.get("value").asText());
        }
        
        if (actionNode.has("url")) {
            action.setUrl(actionNode.get("url").asText());
        }
        
        if (actionNode.has("expectedText")) {
            action.setExpectedText(actionNode.get("expectedText").asText());
        }
        
        if (actionNode.has("expectedCount")) {
            action.setExpectedCount(actionNode.get("expectedCount").asInt());
        }
        
        return action;

    }
    
    /**
     * Parse element locators from JSON node
     */
    private static ElementLocators parseElementLocators(JsonNode elementNode) {
        ElementLocators locators = new ElementLocators();
        
        if (elementNode.has("type")) {
            locators.setType(elementNode.get("type").asText());
        }
        if (elementNode.has("id")) {
            locators.setId(elementNode.get("id").asText());
        }
        if (elementNode.has("name")) {
            locators.setName(elementNode.get("name").asText());
        }
        if (elementNode.has("selector")) {
            locators.setSelector(elementNode.get("selector").asText());
        }
        if (elementNode.has("cssSelector")) {
            locators.setCssSelector(elementNode.get("cssSelector").asText());
        }
        if (elementNode.has("xpath")) {
            locators.setXpath(elementNode.get("xpath").asText());
        }
        if (elementNode.has("text")) {
            locators.setText(elementNode.get("text").asText());
        }
        if (elementNode.has("placeholder")) {
            locators.setPlaceholder(elementNode.get("placeholder").asText());
        }
        if (elementNode.has("dataTest")) {
            locators.setDataTest(elementNode.get("dataTest").asText());
        }
        if (elementNode.has("coordinates")) {
            JsonNode coordsNode = elementNode.get("coordinates");
            locators.setX(coordsNode.get("x").asInt());
            locators.setY(coordsNode.get("y").asInt());
        }
        
        return locators;
    }
}
