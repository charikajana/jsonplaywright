package com.framework.data;

import java.util.List;

/**
 * Feature Data Model - represents a complete feature from JSON
 */
public class FeatureData {
    private String featureName;
    private String scenario;
    private String baseUrl;
    private int totalSteps;
    private int totalActions;
    private List<StepData> steps;
    
    // Getters and Setters
    public String getFeatureName() {
        return featureName;
    }
    
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
    
    public String getScenario() {
        return scenario;
    }
    
    public void setScenario(String scenario) {
        this.scenario = scenario;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getTotalSteps() {
        return totalSteps;
    }
    
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }
    
    public int getTotalActions() {
        return totalActions;
    }
    
    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }
    
    public List<StepData> getSteps() {
        return steps;
    }
    
    public void setSteps(List<StepData> steps) {
        this.steps = steps;
    }
}
