package com.framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to resolve various date string formats into real dates.
 * Supports:
 * - Specific formats: DD-MM-YYYY, DD-MMM-YYYY
 * - Relative formats: "X days", "X months", "X years" (from today)
 */
public class DateResolver {
    private static final Logger logger = LoggerFactory.getLogger(DateResolver.class);
    
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d-M-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    /**
     * Resolves a date string into a formatted output string.
     * 
     * @param dateInput The input string from Gherkin (e.g., "11-01-2026", "25 days")
     * @param outputPattern The pattern to return the date in (e.g., "dd-MM-yyyy")
     * @return The resolved date string
     */
    public static String resolveDate(String dateInput, String outputPattern) {
        if (dateInput == null || dateInput.isEmpty()) return "";
        
        LocalDate resolvedDate = null;
        
        // 1. Try relative date: "X days", "X months", "X years"
        resolvedDate = tryRelativeDate(dateInput);
        
        // 2. Try absolute date formats
        if (resolvedDate == null) {
            resolvedDate = tryAbsoluteDate(dateInput);
        }
        
        if (resolvedDate == null) {
            logger.warn("[DATE] Could not resolve date input: {}. Returning as is.", dateInput);
            return dateInput;
        }

        String result = resolvedDate.format(DateTimeFormatter.ofPattern(outputPattern));
        logger.debug("[DATE] Resolved '{}' to '{}' using pattern '{}'", dateInput, result, outputPattern);
        return result;
    }

    private static LocalDate tryRelativeDate(String input) {
        String lower = input.toLowerCase().trim();
        
        // Handle pure numeric input as days by default
        if (lower.matches("\\d+")) {
            return LocalDate.now().plusDays(Integer.parseInt(lower));
        }

        Pattern pattern = Pattern.compile("(\\d+)\\s*(day|month|year)s?");
        Matcher matcher = pattern.matcher(lower);
        
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "day": return LocalDate.now().plusDays(amount);
                case "month": return LocalDate.now().plusMonths(amount);
                case "year": return LocalDate.now().plusYears(amount);
            }
        }
        return null;
    }

    private static LocalDate tryAbsoluteDate(String input) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
