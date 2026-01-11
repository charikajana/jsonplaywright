package com.framework.executor;

import com.framework.data.*;
import com.framework.playwright.PlaywrightManager;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * JSON-Enhanced Cucumber Executor
 * Uses JSON data collected from Playwright agent to execute steps with precise locators.
 * Delegated action execution to specialized handlers.
 */
public class JsonEnhancedExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JsonEnhancedExecutor.class);
    private final PlaywrightManager playwrightManager;
    private final String featureName;
    
    public JsonEnhancedExecutor(String featureName) {
        this.featureName = featureName;
        this.playwrightManager = PlaywrightManager.getInstance();
        logger.info("[INFO] Initialized No-Code Executor for feature: {}", featureName);
    }
    
    /**
     * Execute a Gherkin step using JSON-provided locators
     */
    public boolean executeStep(String gherkinStep) {
        // Use StepRepository instead of StepDataLoader
        StepData stepData = StepRepository.getStep(gherkinStep);
        
        if (stepData == null) {
            logger.info("[INFO] No JSON data found for step: {}. Searching for traditional step definition...", gherkinStep);
            boolean traditionalSuccess = com.framework.utils.StepDiscoveryRegistry.getInstance().executeMatchingStep(gherkinStep);
            if (traditionalSuccess) {
                logger.info("[SUCCESS] Executed step using traditional step definition");
                return true;
            }
            logger.warn("[WARNING] No JSON or traditional step definition found for: {}", gherkinStep);
            return false;
        }
        
        List<ActionData> actions = stepData.getActions();
        logger.info("[INFO] Executing step: {} ({} actions)", gherkinStep, actions != null ? actions.size() : 0);
        
        if (actions != null) {
            for (ActionData action : actions) {
                boolean actionSuccess = executeAction(action, gherkinStep);
                if (!actionSuccess) {
                    logger.error("[ERROR] Action failed: {} - {}", 
                        action.getActionType(), action.getDescription());
                    return false;
                }
            }
            
            // AFTER SUCCESSFUL EXECUTION: Check if any element was healed
            boolean anyHealed = false;
            for (ActionData action : actions) {
                if (action.getElement() != null && action.getElement().isHealed()) {
                    anyHealed = true;
                    break;
                }
            }
            
            if (anyHealed) {
                logger.info("[HEALING] Persistence: Found healed elements in step. Saving fix to JSON repository...");
                StepRepository.saveOrUpdateStep(stepData);
            }
        }
        
        logger.info("[SUCCESS] Step completed successfully");
        return true;
    }
    
    /**
     * Executes a single action from JSON by delegating to specialized handlers.
     */
    private boolean executeAction(ActionData action, String originalGherkinStep) {
        if (action == null || action.getActionType() == null) {
            logger.warn("[WARNING] Invalid action or action type");
            return true;
        }
        
        Page page = playwrightManager.getPage();
        
        try {
            switch (action.getActionType()) {
                case "NAVIGATE":
                    return NavigationHandler.executeNavigate(page, action, originalGherkinStep);
                    
                case "CLICK":
                    return InteractionHandler.executeClick(page, action);
                    
                case "DOUBLE_CLICK":
                    return InteractionHandler.executeDoubleClick(page, action);
                    
                case "RIGHT_CLICK":
                    return InteractionHandler.executeRightClick(page, action);
                    
                case "TYPE":
                    return InteractionHandler.executeType(page, action, originalGherkinStep);
                    
                case "CLEAR":
                    return InteractionHandler.executeClear(page, action);
                    
                case "SELECT":
                    return InteractionHandler.executeSelect(page, action, originalGherkinStep);
                    
                case "HOVER":
                    return InteractionHandler.executeHover(page, action);
                    
                case "CHECK":
                    return InteractionHandler.executeCheck(page, action);
                    
                case "UNCHECK":
                    return InteractionHandler.executeUncheck(page, action);
                    
                case "PRESS_KEY":
                    return InteractionHandler.executePressKey(page, action, originalGherkinStep);
                    
                case "SWITCH_WINDOW":
                    return NavigationHandler.executeSwitchWindow(page, action, originalGherkinStep);
                    
                case "DRAG_DROP":
                    return InteractionHandler.executeDragDrop(page, action);
                    
                case "SCROLL":
                    return NavigationHandler.executeScroll(page, action);
                    
                case "WAIT_NAVIGATION":
                    return NavigationHandler.executeWaitNavigation(page, action);
                    
                case "VERIFY_TEXT":
                    return VerificationHandler.executeVerifyText(page, action, originalGherkinStep);
                    
                case "VERIFY_ELEMENT":
                    return VerificationHandler.executeVerifyElement(page, action);
                    
                case "VERIFY_ELEMENTS":
                    return VerificationHandler.executeVerifyElements(page, action);
                    
                case "SCREENSHOT":
                    return NavigationHandler.executeScreenshot(page, action);
                    
                default:
                    logger.warn("[WARNING] Unknown action type: {}", action.getActionType());
                    return true; 
            }
        } catch (Exception e) {
            logger.error("[ERROR] Critical failure in action executor: {}", e.getMessage());
            return false;
        }
    }
}
