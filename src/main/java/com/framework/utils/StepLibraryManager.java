package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Step Library Manager - Helps migrate common steps from feature-specific JSON files
 * to shared step libraries for reuse across multiple features
 */
public class StepLibraryManager {
    private static final Logger logger = LoggerFactory.getLogger(StepLibraryManager.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String FEATURE_JSON_DIR = "cache/pages/";
    private static final String SHARED_STEPS_DIR = "cache/shared-steps/";
    
    /**
     * Extract common steps from a feature JSON file and create a shared library
     * 
     * @param featureJsonFile The source feature JSON file
     * @param libraryName Name for the new shared library
     * @param stepGherkinTexts List of Gherkin step texts to extract
     */
    public static void extractStepsToSharedLibrary(
            String featureJsonFile, 
            String libraryName, 
            List<String> stepGherkinTexts) {
        
        try {
            // Read the source feature JSON
            File sourceFile = new File(FEATURE_JSON_DIR + featureJsonFile);
            if (!sourceFile.exists()) {
                logger.error("Source feature JSON not found: {}", sourceFile.getPath());
                return;
            }
            
            JsonNode featureNode = objectMapper.readTree(sourceFile);
            JsonNode stepsArray = featureNode.get("steps");
            
            if (stepsArray == null || !stepsArray.isArray()) {
                logger.error("No steps found in feature JSON");
                return;
            }
            
            // Create shared library JSON structure
            ObjectNode sharedLibrary = objectMapper.createObjectNode();
            sharedLibrary.put("libraryName", libraryName);
            sharedLibrary.put("description", "Shared steps extracted from " + featureJsonFile);
            sharedLibrary.put("createdDate", java.time.LocalDate.now().toString());
            
            ArrayNode extractedSteps = objectMapper.createArrayNode();
            int extractedCount = 0;
            
            // Normalize the target step texts
            Set<String> normalizedTargets = new HashSet<>();
            for (String stepText : stepGherkinTexts) {
                normalizedTargets.add(normalizeStepText(stepText));
            }
            
            // Extract matching steps
            for (JsonNode stepNode : stepsArray) {
                String gherkinStep = stepNode.get("gherkinStep").asText();
                String normalized = normalizeStepText(gherkinStep);
                
                if (normalizedTargets.contains(normalized)) {
                    extractedSteps.add(stepNode);
                    extractedCount++;
                    logger.info("[OK] Extracted step: {}", gherkinStep);
                }
            }
            
            if (extractedCount == 0) {
                logger.warn("No matching steps found to extract");
                return;
            }
            
            sharedLibrary.set("steps", extractedSteps);
            
            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("totalSteps", extractedCount);
            metadata.put("sourceFeature", featureJsonFile);
            sharedLibrary.set("metadata", metadata);
            
            // Save the shared library
            File outputFile = new File(SHARED_STEPS_DIR + libraryName + ".json");
            Files.createDirectories(Paths.get(SHARED_STEPS_DIR));
            
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(outputFile, sharedLibrary);
            
            logger.info("[OK] Created shared library: {} with {} steps", 
                       outputFile.getPath(), extractedCount);
            
        } catch (IOException e) {
            logger.error("Error creating shared library: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Merge steps from multiple feature files into a single shared library
     * 
     * @param features Map of feature file name to list of step texts to extract
     * @param libraryName Name for the shared library
     */
    public static void mergeStepsFromMultipleFeatures(
            Map<String, List<String>> features,
            String libraryName) {
        
        try {
            ObjectNode sharedLibrary = objectMapper.createObjectNode();
            sharedLibrary.put("libraryName", libraryName);
            sharedLibrary.put("description", "Shared steps merged from multiple features");
            sharedLibrary.put("createdDate", java.time.LocalDate.now().toString());
            
            ArrayNode allSteps = objectMapper.createArrayNode();
            Set<String> addedSteps = new HashSet<>(); // Prevent duplicates
            int totalExtracted = 0;
            
            // Process each feature
            for (Map.Entry<String, List<String>> entry : features.entrySet()) {
                String featureFile = entry.getKey();
                List<String> stepTexts = entry.getValue();
                
                File sourceFile = new File(FEATURE_JSON_DIR + featureFile);
                if (!sourceFile.exists()) {
                    logger.warn("Feature file not found: {}", sourceFile.getPath());
                    continue;
                }
                
                JsonNode featureNode = objectMapper.readTree(sourceFile);
                JsonNode stepsArray = featureNode.get("steps");
                
                if (stepsArray == null || !stepsArray.isArray()) {
                    continue;
                }
                
                // Normalize target steps
                Set<String> normalizedTargets = new HashSet<>();
                for (String stepText : stepTexts) {
                    normalizedTargets.add(normalizeStepText(stepText));
                }
                
                // Extract and merge
                for (JsonNode stepNode : stepsArray) {
                    String gherkinStep = stepNode.get("gherkinStep").asText();
                    String normalized = normalizeStepText(gherkinStep);
                    
                    if (normalizedTargets.contains(normalized) && !addedSteps.contains(normalized)) {
                        allSteps.add(stepNode);
                        addedSteps.add(normalized);
                        totalExtracted++;
                        logger.info("[OK] Merged step from {}: {}", featureFile, gherkinStep);
                    }
                }
            }
            
            if (totalExtracted == 0) {
                logger.warn("No steps were extracted from any feature");
                return;
            }
            
            sharedLibrary.set("steps", allSteps);
            
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("totalSteps", totalExtracted);
            metadata.put("sourceFeatures", String.join(", ", features.keySet()));
            sharedLibrary.set("metadata", metadata);
            
            // Save
            File outputFile = new File(SHARED_STEPS_DIR + libraryName + ".json");
            Files.createDirectories(Paths.get(SHARED_STEPS_DIR));
            
            objectMapper.writerWithDefaultPrettyPrinter()
                       .writeValue(outputFile, sharedLibrary);
            
            logger.info("[OK] Created merged shared library: {} with {} unique steps", 
                       outputFile.getPath(), totalExtracted);
            
        } catch (IOException e) {
            logger.error("Error merging shared library: {}", e.getMessage(), e);
        }
    }
    
    /**
     * List all steps in a feature JSON file
     */
    public static List<String> listStepsInFeature(String featureJsonFile) {
        List<String> steps = new ArrayList<>();
        
        try {
            File sourceFile = new File(FEATURE_JSON_DIR + featureJsonFile);
            if (!sourceFile.exists()) {
                logger.error("Feature file not found: {}", sourceFile.getPath());
                return steps;
            }
            
            JsonNode featureNode = objectMapper.readTree(sourceFile);
            JsonNode stepsArray = featureNode.get("steps");
            
            if (stepsArray != null && stepsArray.isArray()) {
                for (JsonNode stepNode : stepsArray) {
                    String gherkinStep = stepNode.get("gherkinStep").asText();
                    steps.add(gherkinStep);
                }
            }
            
        } catch (IOException e) {
            logger.error("Error reading feature file: {}", e.getMessage());
        }
        
        return steps;
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
     * Example usage demonstrating how to extract common steps
     */
    public static void main(String[] args) {
        logger.info("=== Step Library Manager ===");
        
        // Example 1: Extract login steps from ProductSearch feature
        List<String> loginSteps = Arrays.asList(
            "Given \"Open the application 'https://www.saucedemo.com/'\"",
            "When \"Type 'standard_user' into username\"",
            "And \"Type 'secret_sauce' into password\"",
            "And \"Click on Login button\""
        );
        
        extractStepsToSharedLibrary(
            "ProductSearch-feature.json",
            "common-login",
            loginSteps
        );
        
        // Example 2: Merge common steps from multiple features
        Map<String, List<String>> featureSteps = new HashMap<>();
        featureSteps.put("ProductSearch-feature.json", loginSteps);
        featureSteps.put("Login-feature.json", loginSteps);
        
        mergeStepsFromMultipleFeatures(featureSteps, "common-auth");
        
        logger.info("=== Done ===");
    }
}
