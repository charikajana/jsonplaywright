package com.framework.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Shared Steps Provider - Manages reusable step definitions across multiple features
 * Allows steps to be defined once and reused in multiple feature files
 */
public class SharedStepsProvider {
    private static final Logger logger = LoggerFactory.getLogger(SharedStepsProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Cache for shared step libraries
    private static final Map<String, Map<String, StepData>> sharedStepsCache = new HashMap<>();
    
    private static final String SHARED_STEPS_DIR = "cache/shared-steps/";
    
    /**
     * Load shared steps from a library file
     * @param libraryName Name of the shared steps library (e.g., "common-login", "common-navigation")
     * @return Map of step text to StepData
     */
    public static Map<String, StepData> loadSharedStepsLibrary(String libraryName) {
        if (sharedStepsCache.containsKey(libraryName)) {
            return sharedStepsCache.get(libraryName);
        }
        
        try {
            String jsonFileName = libraryName + ".json";
            File jsonFile = new File(SHARED_STEPS_DIR + jsonFileName);
            
            if (!jsonFile.exists()) {
                logger.warn("Shared steps library not found: {}", jsonFile.getPath());
                return Collections.emptyMap();
            }
            
            logger.info("Loading shared steps from: {}", jsonFile.getPath());
            JsonNode rootNode = objectMapper.readTree(jsonFile);
            
            Map<String, StepData> stepsMap = parseSharedSteps(rootNode);
            sharedStepsCache.put(libraryName, stepsMap);
            
            logger.info("Loaded {} shared steps from library: {}", stepsMap.size(), libraryName);
            
            return stepsMap;
            
        } catch (IOException e) {
            logger.error("Error loading shared steps library: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
    
    /**
     * Get step data from shared library
     * @param libraryName Name of the library
     * @param gherkinStep The step text to lookup
     * @return StepData if found, null otherwise
     */
    public static StepData getSharedStep(String libraryName, String gherkinStep) {
        Map<String, StepData> library = loadSharedStepsLibrary(libraryName);
        if (library.isEmpty()) {
            return null;
        }
        
        String normalizedStep = normalizeStepText(gherkinStep);
        return library.get(normalizedStep);
    }
    
    /**
     * Get step data from multiple shared libraries
     * Searches in order and returns the first match
     */
    public static StepData getSharedStep(List<String> libraryNames, String gherkinStep) {
        for (String libraryName : libraryNames) {
            StepData stepData = getSharedStep(libraryName, gherkinStep);
            if (stepData != null) {
                logger.debug("Found step in shared library: {}", libraryName);
                return stepData;
            }
        }
        return null;
    }
    
    /**
     * Get element locators from shared steps
     */
    public static ElementLocators getLocatorsFromSharedStep(String libraryName, String gherkinStep) {
        StepData stepData = getSharedStep(libraryName, gherkinStep);
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
     * Get actions from shared steps
     */
    public static List<ActionData> getActionsFromSharedStep(String libraryName, String gherkinStep) {
        StepData stepData = getSharedStep(libraryName, gherkinStep);
        if (stepData == null) {
            return Collections.emptyList();
        }
        return stepData.getActions();
    }
    
    /**
     * Parse shared steps from JSON
     */
    private static Map<String, StepData> parseSharedSteps(JsonNode rootNode) {
        Map<String, StepData> stepsMap = new HashMap<>();
        
        JsonNode stepsNode = rootNode.get("steps");
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                StepData stepData = parseStepData(stepNode);
                String normalizedStep = normalizeStepText(stepData.getGherkinStep());
                stepsMap.put(normalizedStep, stepData);
            }
        }
        
        return stepsMap;
    }
    
    /**
     * Parse step data from JSON node (reuses JsonDataProvider logic)
     */
    private static StepData parseStepData(JsonNode stepNode) {
        StepData step = new StepData();
        step.setStepNumber(stepNode.get("stepNumber").asInt());
        step.setGherkinStep(stepNode.get("gherkinStep").asText());
        step.setStepType(stepNode.get("stepType").asText());
        step.setStatus(stepNode.get("status").asText());
        
        List<ActionData> actions = new ArrayList<>();
        JsonNode actionsNode = stepNode.get("actions");
        
        if (actionsNode.isArray()) {
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
    
    /**
     * Normalize step text for comparison
     */
    private static String normalizeStepText(String stepText) {
        return stepText
            .replaceAll("^(Given|When|Then|And|But)\\s+", "")
            .replaceAll("\"", "'")
            .trim()
            .toLowerCase();
    }
    
    /**
     * Clear cache (useful for testing)
     */
    public static void clearCache() {
        sharedStepsCache.clear();
        logger.info("Shared steps cache cleared");
    }
}
