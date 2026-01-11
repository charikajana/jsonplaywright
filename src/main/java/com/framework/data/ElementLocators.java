package com.framework.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Element Locators Model - stores all possible locator strategies for an element
 * Populated from Playwright agent execution results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private String ariaLabel;     // ARIA label
    private String role;          // ARIA role
    private String title;         // Title attribute
    private String alt;           // Alt attribute for images
    private String className;     // CSS class list
    private String value;         // Current value (if applicable)
    private String href;          // Link destination
    private String src;           // Image source
    private Coordinates coordinates; // Click coordinates
    private FingerprintData fingerprint; // Self-healing DNA
    
    /**
     * Get the best available locator in order of preference
     */
    @JsonIgnore
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
    @JsonIgnore
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
    @JsonIgnore
    public boolean hasCoordinates() {
        return coordinates != null && coordinates.getX() > 0 && coordinates.getY() > 0;
    }
    
    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }

    @JsonIgnore
    public int getX() { return coordinates != null ? coordinates.getX() : 0; }
    @JsonIgnore
    public int getY() { return coordinates != null ? coordinates.getY() : 0; }
    
    public void setX(int x) { 
        if (coordinates == null) coordinates = new Coordinates();
        coordinates.setX(x);
    }
    public void setY(int y) { 
        if (coordinates == null) coordinates = new Coordinates();
        coordinates.setY(y);
    }
    
    // Getters and Setters
    public FingerprintData getFingerprint() { return fingerprint; }
    public void setFingerprint(FingerprintData fingerprint) { this.fingerprint = fingerprint; }

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
    
    public String getAriaLabel() { return ariaLabel; }
    public void setAriaLabel(String ariaLabel) { this.ariaLabel = ariaLabel; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAlt() { return alt; }
    public void setAlt(String alt) { this.alt = alt; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }

    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }

    public static class Coordinates {
        private int x;
        private int y;

        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }

    private boolean healed = false; // Flag to track if element was fixed during this run

    @Override
    public String toString() {
        return "ElementLocators{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", text='" + text + '\'' +
                ", healed=" + healed +
                '}';
    }

    public boolean isHealed() { return healed; }
    public void setHealed(boolean healed) { this.healed = healed; }

    // Nested classes for Self-Healing
    public static class FingerprintData {
        private Attributes attributes;
        private Context context;

        public Attributes getAttributes() { return attributes; }
        public void setAttributes(Attributes attributes) { this.attributes = attributes; }

        public Context getContext() { return context; }
        public void setContext(Context context) { this.context = context; }
    }

    public static class Attributes {
        private String type;
        private String ariaLabel;
        private String classList;
        private String role;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAriaLabel() { return ariaLabel; }
        public void setAriaLabel(String ariaLabel) { this.ariaLabel = ariaLabel; }
        public String getClassList() { return classList; }
        public void setClassList(String classList) { this.classList = classList; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class Context {
        private String parentTag;
        private String parentClass;
        private String nearbyText;
        private String heading;

        public String getParentTag() { return parentTag; }
        public void setParentTag(String parentTag) { this.parentTag = parentTag; }
        public String getParentClass() { return parentClass; }
        public void setParentClass(String parentClass) { this.parentClass = parentClass; }
        public String getNearbyText() { return nearbyText; }
        public void setNearbyText(String nearbyText) { this.nearbyText = nearbyText; }
        public String getHeading() { return heading; }
        public void setHeading(String heading) { this.heading = heading; }
    }
}
