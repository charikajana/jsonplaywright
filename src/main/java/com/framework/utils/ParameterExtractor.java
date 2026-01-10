package com.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting parameters from Gherkin steps.
 * Supports single and multiple parameter extraction from quoted strings.
 * 
 * Example:
 * - "Type 'student' into username" -> ["student"]
 * - "Fill 'John' as firstName and 'Doe' as lastName" -> ["John", "Doe"]
 */
public class ParameterExtractor {
    private static final Logger logger = LoggerFactory.getLogger(ParameterExtractor.class);
    private static final Pattern QUOTED_PATTERN = Pattern.compile("\"([^\"]*)\"");
    
    /**
     * Extracts the first parameter found between double quotes in a Gherkin step.
     * 
     * @param gherkinStep The Gherkin step text
     * @return The first parameter value, or null if no parameters found
     * 
     * Example: "Type 'student' into username" -> "student"
     */
    public static String extractFirstParameter(String gherkinStep) {
        if (gherkinStep == null || gherkinStep.isEmpty()) {
            return null;
        }
        
        Matcher matcher = QUOTED_PATTERN.matcher(gherkinStep);
        if (matcher.find()) {
            String param = matcher.group(1);
            logger.debug("[PARAM] Extracted first parameter: '{}'", param);
            return param;
        }
        
        logger.debug("[PARAM] No parameters found in step: {}", gherkinStep);
        return null;
    }
    
    /**
     * Extracts all parameters found between double quotes in a Gherkin step.
     * 
     * @param gherkinStep The Gherkin step text
     * @return List of all parameter values (empty list if none found)
     * 
     * Example: "Fill 'John' as firstName and 'Doe' as lastName" -> ["John", "Doe"]
     */
    public static List<String> extractAllParameters(String gherkinStep) {
        List<String> parameters = new ArrayList<>();
        
        if (gherkinStep == null || gherkinStep.isEmpty()) {
            return parameters;
        }
        
        Matcher matcher = QUOTED_PATTERN.matcher(gherkinStep);
        while (matcher.find()) {
            String param = matcher.group(1);
            parameters.add(param);
            logger.debug("[PARAM] Extracted parameter {}: '{}'", parameters.size(), param);
        }
        
        if (parameters.isEmpty()) {
            logger.debug("[PARAM] No parameters found in step: {}", gherkinStep);
        } else {
            logger.debug("[PARAM] Total {} parameter(s) extracted from step", parameters.size());
        }
        
        return parameters;
    }
    
    /**
     * Extracts a parameter at a specific index.
     * 
     * @param gherkinStep The Gherkin step text
     * @param index Zero-based index of the parameter to extract
     * @return The parameter value at the specified index, or null if not found
     * 
     * Example: extractParameterAtIndex("Fill 'John' and 'Doe'", 0) -> "John"
     */
    public static String extractParameterAtIndex(String gherkinStep, int index) {
        List<String> parameters = extractAllParameters(gherkinStep);
        
        if (index < 0 || index >= parameters.size()) {
            logger.warn("[PARAM] Index {} out of bounds. Total parameters: {}", 
                index, parameters.size());
            return null;
        }
        
        return parameters.get(index);
    }
    
    /**
     * Counts the number of parameters in a Gherkin step.
     * 
     * @param gherkinStep The Gherkin step text
     * @return The number of parameters found
     */
    public static int countParameters(String gherkinStep) {
        return extractAllParameters(gherkinStep).size();
    }
    
    /**
     * Checks if a Gherkin step contains any parameters.
     * 
     * @param gherkinStep The Gherkin step text
     * @return true if at least one parameter exists, false otherwise
     */
    public static boolean hasParameters(String gherkinStep) {
        return countParameters(gherkinStep) > 0;
    }
}
