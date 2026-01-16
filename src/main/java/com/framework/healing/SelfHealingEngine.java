package com.framework.healing;

import com.framework.data.ElementLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Self-Healing Engine - The "Medical Kit" for broken tests.
 * This class is triggered only when standard locators fail.
 * It uses the element's DNA (Fingerprint) to re-discover it on the page.
 */
public class SelfHealingEngine {
    private static final Logger logger = LoggerFactory.getLogger(SelfHealingEngine.class);

    public static class HealedResult {
        public final Locator locator;
        public final String selector;
        public HealedResult(Locator locator, String selector) {
            this.locator = locator;
            this.selector = selector;
        }
    }

    /**
     * Attempts to heal a broken locator using fingerprint metadata.
     */
    public static HealedResult attemptHealing(Page page, ElementLocators locators) {
        if (locators.getFingerprint() == null) {
            logger.warn("[HEALING] No fingerprint DNA found for element. Cannot heal.");
            return null;
        }

        logger.info("****************************************************");
        logger.info("[HEALING] SELF-HEALING TRIGGERED for: {}", locators.toString());
        logger.info("****************************************************");

        ElementLocators.FingerprintData dna = locators.getFingerprint();
        
        // Strategy 1: Label Search
        HealedResult result = tryLabelHeal(page, dna);
        if (result == null) result = trySemanticHeal(page, dna);
        if (result == null) result = tryProximityHeal(page, dna, locators.getType());
        if (result == null) result = tryFuzzyAttributeHeal(page, dna);
        
        if (result != null) {
            logger.info("[HEALING] [SUCCESS] Element rediscovered using healing model.");
        } else {
            logger.error("[HEALING] [FAILED] DNA match not found on current page.");
        }
        
        return result;
    }

    private static HealedResult tryLabelHeal(Page page, ElementLocators.FingerprintData dna) {
        ElementLocators.Context context = dna.getContext();
        if (context == null || context.getNearbyText() == null) return null;

        try {
            logger.info("[HEALING] Trying Strategy 1: Label Match (Label: '{}')", context.getNearbyText());
            Locator candidate = page.getByLabel(context.getNearbyText());
            if (candidate.count() == 1) {
                logger.info("[HEALING] [OK] Match found by Label!");
                // Use Playwright internal selector format for persistence
                return new HealedResult(candidate, "internal:label=\"" + context.getNearbyText() + "\"i");
            }
        } catch (Exception e) {
            logger.debug("[HEALING] Label match failed: {}", e.getMessage());
        }
        return null;
    }

    private static HealedResult trySemanticHeal(Page page, ElementLocators.FingerprintData dna) {
        ElementLocators.Attributes attr = dna.getAttributes();
        if (attr == null || attr.getRole() == null) return null;

        try {
            logger.info("[HEALING] Trying Strategy 2: Semantic Match (Role: {}, Name: {})", 
                attr.getRole(), attr.getAriaLabel());
            
            AriaRole role = AriaRole.valueOf(attr.getRole().toUpperCase());
            Page.GetByRoleOptions options = new Page.GetByRoleOptions();
            if (attr.getAriaLabel() != null) options.setName(attr.getAriaLabel());
            
            Locator candidate = page.getByRole(role, options);
            if (candidate.count() == 1) {
                logger.info("[HEALING] [OK] Semantic match found!");
                String selector = "role=" + attr.getRole().toLowerCase();
                if (attr.getAriaLabel() != null) selector += "[name=\"" + attr.getAriaLabel() + "\"]";
                return new HealedResult(candidate, selector);
            }
        } catch (Exception e) {
            logger.debug("[HEALING] Semantic match failed: {}", e.getMessage());
        }
        return null;
    }

    private static HealedResult tryProximityHeal(Page page, ElementLocators.FingerprintData dna, String originalType) {
        ElementLocators.Context context = dna.getContext();
        if (context == null || context.getNearbyText() == null) return null;

        try {
            logger.info("[HEALING] Trying Strategy 3: Proximity Search (Anchor: '{}')", context.getNearbyText());
            String tag = (originalType != null && !originalType.isEmpty()) ? originalType : "input, button, select";
            
            // Try relative selectors
            String nearSelector = tag + ":near(:text(\"" + context.getNearbyText() + "\"))";
            Locator candidate = page.locator(nearSelector).first();
            if (candidate.count() > 0) {
               logger.info("[HEALING] [OK] Proximity match found using :near relative selector!");
               return new HealedResult(candidate, nearSelector);
            }
        } catch (Exception e) {
            logger.debug("[HEALING] Proximity match failed: {}", e.getMessage());
        }
        return null;
    }

    private static HealedResult tryFuzzyAttributeHeal(Page page, ElementLocators.FingerprintData dna) {
        ElementLocators.Attributes attr = dna.getAttributes();
        if (attr == null || attr.getClassList() == null || attr.getClassList().trim().isEmpty()) return null;

        logger.info("[HEALING] Trying Strategy 4: Fuzzy Attribute Match (Classes: {})", attr.getClassList());
        String[] classes = attr.getClassList().split(" ");
        for (String cls : classes) {
            cls = cls.trim();
            if (cls.isEmpty() || cls.length() < 3) continue;
            try {
                Locator candidate = page.locator("." + cls);
                if (candidate.count() == 1) {
                    logger.info("[HEALING] [OK] Found unique element matching class: {}", cls);
                    return new HealedResult(candidate, "." + cls);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}
