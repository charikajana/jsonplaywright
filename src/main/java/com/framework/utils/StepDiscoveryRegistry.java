package com.framework.utils;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registry that discovers and executes traditional Cucumber step definitions.
 * This is used as a fallback when no JSON locator data is found for a step.
 */
public class StepDiscoveryRegistry {
    private static final Logger logger = LoggerFactory.getLogger(StepDiscoveryRegistry.class);
    private static StepDiscoveryRegistry instance;
    
    private final Map<Pattern, StepMethod> stepMethods = new HashMap<>();
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private StepDiscoveryRegistry() {
        // Automatically scan the framework steps package (not in Cucumber glue)
        scanPackage("com.framework.steps");
    }

    public static synchronized StepDiscoveryRegistry getInstance() {
        if (instance == null) {
            instance = new StepDiscoveryRegistry();
        }
        return instance;
    }

    /**
     * Scans a package for Cucumber-annotated methods
     */
    public void scanPackage(String packageName) {
        try {
            List<Class<?>> classes = getClasses(packageName);
            for (Class<?> clazz : classes) {
                // Skip the UniversalStepDefinition to avoid infinite recursion
                if (clazz.getSimpleName().equals("UniversalStepDefinition")) continue;
                
                for (Method method : clazz.getDeclaredMethods()) {
                    String patternStr = getCucumberPattern(method);
                    if (patternStr != null) {
                        // Convert Cucumber expression to regex
                        String regex = convertToRegex(patternStr);
                        logger.info("[DISCOVERY] Found step definition: {} -> {}.{}", 
                                patternStr, clazz.getSimpleName(), method.getName());
                        stepMethods.put(Pattern.compile(regex), new StepMethod(clazz, method));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR] Failed to scan package {}: {}", packageName, e.getMessage());
        }
    }

    /**
     * Executes a matching step definition if found
     */
    public boolean executeMatchingStep(String gherkinStep) {
        String cleanStep = stripGherkinKeyword(gherkinStep);
        
        for (Map.Entry<Pattern, StepMethod> entry : stepMethods.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(cleanStep);
            
            if (matcher.matches()) {
                StepMethod stepMethod = entry.getValue();
                return invokeMethod(stepMethod, matcher);
            }
        }
        
        logger.warn("[WARNING] No traditional step definition found for: {}", gherkinStep);
        return false;
    }

    private boolean invokeMethod(StepMethod stepMethod, Matcher matcher) {
        try {
            Object instance = instanceCache.computeIfAbsent(stepMethod.clazz, c -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate step class: " + c.getName(), e);
                }
            });

            Object[] args = new Object[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                args[i] = matcher.group(i + 1);
            }

            logger.info("[EXECUTE] Invoking traditional step: {}.{}", 
                    stepMethod.clazz.getSimpleName(), stepMethod.method.getName());
            
            stepMethod.method.invoke(instance, args);
            return true;
        } catch (Exception e) {
            logger.error("[ERROR] Step invocation failed: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return false;
        }
    }

    private String getCucumberPattern(Method method) {
        if (method.isAnnotationPresent(Given.class)) return method.getAnnotation(Given.class).value();
        if (method.isAnnotationPresent(When.class)) return method.getAnnotation(When.class).value();
        if (method.isAnnotationPresent(Then.class)) return method.getAnnotation(Then.class).value();
        return null;
    }

    private String stripGherkinKeyword(String step) {
        return step.replaceAll("^(Given|When|Then|And|But)\\s+", "").trim();
    }

    private List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile().replace("%20", " ")));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private String convertToRegex(String cucumberExpression) {
        // If it starts with ^ and ends with $, assume it's already a regex
        if (cucumberExpression.startsWith("^") && cucumberExpression.endsWith("$")) {
            return cucumberExpression;
        }

        String regex = cucumberExpression;
        // Escape standard regex characters
        regex = regex.replace("(", "\\(").replace(")", "\\)")
                     .replace("[", "\\[").replace("]", "\\]")
                     .replace("?", "\\?").replace("*", "\\*")
                     .replace("+", "\\+").replace(".", "\\.");

        // Replace cucumber placeholders with regex groups
        regex = regex.replace("{string}", "\"([^\"]*)\"")
                     .replace("{int}", "(\\d+)")
                     .replace("{float}", "(\\d*\\.?\\d+)")
                     .replace("{word}", "(\\w+)");

        // Add start and end anchors
        return "^" + regex + "$";
    }

    private static class StepMethod {
        final Class<?> clazz;
        final Method method;

        StepMethod(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
        }
    }
}
