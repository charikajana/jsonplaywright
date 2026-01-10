package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.data.ActionData;
import com.framework.data.StepData;
import com.framework.data.StepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Populates the Step Repository from existing feature JSON files
 * Extracts each step and saves it as an individual file
 */
public class StepRepositoryPopulator {
    private static final Logger logger = LoggerFactory.getLogger(StepRepositoryPopulator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String FEATURE_JSON_DIR = "cache/pages/";
    
    /**
     * Populate repository from a single feature JSON file
     * @param featureJsonFile The feature JSON file name
     * @return Number of steps added to repository
     */
    public static int populateFromFeature(String featureJsonFile) {
        logger.info("Processing feature file: {}", featureJsonFile);
        
        try {
            File sourceFile = new File(FEATURE_JSON_DIR + featureJsonFile);
            if (!sourceFile.exists()) {
                logger.error("Feature file not found: {}", sourceFile.getPath());
                return 0;
            }
            
            JsonNode featureNode = objectMapper.readTree(sourceFile);
            JsonNode stepsArray = featureNode.get("steps");
            
            if (stepsArray == null || !stepsArray.isArray()) {
                logger.warn("No steps found in feature file");
                return 0;
            }
            
            int savedCount = 0;
            int skippedCount = 0;
            
            for (JsonNode stepNode : stepsArray) {
                StepData stepData = parseStepData(stepNode);
                
                // Check if step already exists
                if (StepRepository.stepExists(stepData.getGherkinStep())) {
                    logger.debug("Step already exists, skipping: {}", stepData.getGherkinStep());
                    skippedCount++;
                    continue;
                }
                
                // Save to repository
                boolean saved = StepRepository.saveStep(stepData);
                if (saved) {
                    savedCount++;
                } else {
                    logger.warn("Failed to save step: {}", stepData.getGherkinStep());
                }
            }
            
            logger.info("[OK] Processed {}: {} steps saved, {} skipped (already exist)", 
                       featureJsonFile, savedCount, skippedCount);
            
            return savedCount;
            
        } catch (IOException e) {
            logger.error("Error processing feature file: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Populate repository from all feature JSON files in the directory
     * @return Total number of steps added to repository
     */
    public static int populateFromAllFeatures() {
        logger.info("=== Populating Step Repository from All Features ===");
        
        File dir = new File(FEATURE_JSON_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.error("Feature JSON directory not found: {}", FEATURE_JSON_DIR);
            return 0;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            logger.warn("No feature JSON files found in directory");
            return 0;
        }
        
        int totalSaved = 0;
        for (File file : files) {
            int saved = populateFromFeature(file.getName());
            totalSaved += saved;
        }
        
        logger.info("=== Summary: {} unique steps added to repository ===", totalSaved);
        logger.info("Repository location: cache/steps/");
        
        // List total steps in repository
        List<String> allSteps = StepRepository.listAllSteps();
        logger.info("Total steps in repository: {}", allSteps.size());
        
        return totalSaved;
    }
    
    /**
     * Populate repository from specific feature files
     * @param featureFiles List of feature file names
     * @return Total number of steps added
     */
    public static int populateFromFeatures(List<String> featureFiles) {
        logger.info("=== Populating Step Repository from Selected Features ===");
        
        int totalSaved = 0;
        for (String featureFile : featureFiles) {
            int saved = populateFromFeature(featureFile);
            totalSaved += saved;
        }
        
        logger.info("=== Summary: {} unique steps added to repository ===", totalSaved);
        return totalSaved;
    }
    
    /**
     * Parse step data from JSON node
     */
    private static StepData parseStepData(JsonNode stepNode) {
        StepData step = new StepData();
        step.setStepNumber(stepNode.get("stepNumber").asInt());
        step.setGherkinStep(stepNode.get("gherkinStep").asText());
        step.setStepType(stepNode.get("stepType").asText());
        step.setStatus(stepNode.get("status").asText());
        
        List<ActionData> actions = new ArrayList<>();
        JsonNode actionsNode = stepNode.get("actions");
        
        if (actionsNode != null && actionsNode.isArray()) {
            for (JsonNode actionNode : actionsNode) {
                ActionData action = objectMapper.convertValue(actionNode, ActionData.class);
                actions.add(action);
            }
        }
        
        step.setActions(actions);
        return step;
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        logger.info("Step Repository Populator - Starting...");
        
        if (args.length > 0) {
            // Populate from specific files
            List<String> featureFiles = List.of(args);
            populateFromFeatures(featureFiles);
        } else {
            // Populate from all feature files
            populateFromAllFeatures();
        }
        
        logger.info("Done!");
    }
}
