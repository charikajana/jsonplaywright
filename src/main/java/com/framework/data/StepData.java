package com.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Step Data Model - represents a single step with its actions
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StepData {
    private int stepNumber;
    private String gherkinStep;
    private String stepType;
    private String status;
    private List<ActionData> actions;
    
    // Getters and Setters
    public int getStepNumber() {
        return stepNumber;
    }
    
    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }
    
    public String getGherkinStep() {
        return gherkinStep;
    }
    
    public void setGherkinStep(String gherkinStep) {
        this.gherkinStep = gherkinStep;
    }
    
    public String getStepType() {
        return stepType;
    }
    
    public void setStepType(String stepType) {
        this.stepType = stepType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<ActionData> getActions() {
        return actions;
    }
    
    public void setActions(List<ActionData> actions) {
        this.actions = actions;
    }
}
