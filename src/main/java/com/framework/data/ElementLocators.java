package com.framework.data;

/**
 * Element Locators Model - stores all possible locator strategies for an element
 * Populated from Playwright agent execution results
 */
public class ElementLocators {
    private String type;          // input, button, div, etc.
    private String id;            // Element ID
    private String name;          // Element name attribute
    private String selector;      // Primary CSS selector
    private String cssSelector;   // Full CSS selector
    private String xpath;         // XPath locator
    private String text;          // Button/link text
    private String placeholder;   // Input placeholder
    private String dataTest;      // data-test attribute
    private int x;                // X coordinate
    private int y;                // Y coordinate
    
    /**
     * Get the best available locator in order of preference
     */
    public String getBestLocator() {
        // Priority: ID > data-test > selector > cssSelector > xpath > text
        if (id != null && !id.isEmpty() && !id.equalsIgnoreCase("null")) {
            return "#" + id;
        }
        if (dataTest != null && !dataTest.isEmpty() && !dataTest.equalsIgnoreCase("null")) {
            return "[data-test='" + dataTest + "']";
        }
        if (selector != null && !selector.isEmpty() && !selector.equalsIgnoreCase("null")) {
            return selector;
        }
        if (cssSelector != null && !cssSelector.isEmpty() && !cssSelector.equalsIgnoreCase("null")) {
            return cssSelector;
        }
        if (xpath != null && !xpath.isEmpty() && !xpath.equalsIgnoreCase("null")) {
            return xpath;
        }
        if (text != null && !text.isEmpty() && !text.equalsIgnoreCase("null")) {
            return "text=" + text;
        }
        return null;
    }
    
    /**
     * Get Playwright-compatible locator
     */
    public String getPlaywrightLocator() {
        if (id != null && !id.isEmpty()) {
            return "id=" + id;
        }
        if (dataTest != null && !dataTest.isEmpty()) {
            return "[data-test='" + dataTest + "']";
        }
        if (selector != null && !selector.isEmpty()) {
            return selector;
        }
        if (text != null && !text.isEmpty()) {
            return "text=" + text;
        }
        return cssSelector;
    }
    
    /**
     * Check if element has coordinates
     */
    public boolean hasCoordinates() {
        return x > 0 && y > 0;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSelector() {
        return selector;
    }
    
    public void setSelector(String selector) {
        this.selector = selector;
    }
    
    public String getCssSelector() {
        return cssSelector;
    }
    
    public void setCssSelector(String cssSelector) {
        this.cssSelector = cssSelector;
    }
    
    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
    
    public String getDataTest() {
        return dataTest;
    }
    
    public void setDataTest(String dataTest) {
        this.dataTest = dataTest;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "ElementLocators{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
