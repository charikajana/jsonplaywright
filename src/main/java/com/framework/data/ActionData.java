package com.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Action Data Model - represents a single action within a step
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionData {
    private int actionNumber;
    private String actionType;
    private String description;
    private ElementLocators element;
    private ElementLocators targetElement;  // For drag-drop actions
    private String value;
    private String url;
    private String expectedText;
    private Integer expectedCount;
    private String comparisonType; // EXACTLY or CONTAINS
    private String status;

    
    // Getters and Setters
    public int getActionNumber() {
        return actionNumber;
    }
    
    public void setActionNumber(int actionNumber) {
        this.actionNumber = actionNumber;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ElementLocators getElement() {
        return element;
    }
    
    public void setElement(ElementLocators element) {
        this.element = element;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getExpectedText() {
        return expectedText;
    }
    
    public void setExpectedText(String expectedText) {
        this.expectedText = expectedText;
    }
    
    public Integer getExpectedCount() {
        return expectedCount;
    }
    
    public void setExpectedCount(Integer expectedCount) {
        this.expectedCount = expectedCount;
    }
    
    public String getComparisonType() {
        return comparisonType;
    }
    
    public void setComparisonType(String comparisonType) {
        this.comparisonType = comparisonType;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public ElementLocators getTargetElement() {
        return targetElement;
    }
    
    public void setTargetElement(ElementLocators targetElement) {
        this.targetElement = targetElement;
    }

}
