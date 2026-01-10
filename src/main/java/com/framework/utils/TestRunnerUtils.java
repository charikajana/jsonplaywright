package com.framework.utils;

import com.framework.config.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class TestRunnerUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestRunnerUtils.class);

    public static String getTimestampedPath(String base) {
        LocalDateTime now = LocalDateTime.now();
        String dateFolder = now.format(DateTimeFormatter.ofPattern("ddMMM")).toUpperCase();
        String timeFolder = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        return base + "/" + dateFolder + "/" + timeFolder;
    }

    public static void createAllureEnvironmentFile(String resultsPath) {
        try {
            Files.createDirectories(Paths.get(resultsPath));
            Properties props = new Properties();
            
            // Capture Browser - Priority: System Prop (-DBrowserName) > Config File > Default
            String browser = System.getProperty("BrowserName");
            if (browser == null) {
                browser = ExecutionConfig.getInstance().getBrowserType();
            }
            
            // Capture Environment - Priority: System Prop (-DEnv) > Default
            String env = System.getProperty("Env");
            if (env == null) {
                env = "QA"; // Default if not provided
            }
            
            props.setProperty("OS", System.getProperty("os.name"));
            props.setProperty("Java.Version", System.getProperty("java.version"));
            props.setProperty("Browser", browser);
            props.setProperty("Environment", env);
            props.setProperty("Framework", "No-Code Playwright Agent");

            try (FileOutputStream fos = new FileOutputStream(resultsPath + "/environment.properties")) {
                props.store(fos, "Allure Environment Properties");
            }
            logger.info("[SUCCESS] Allure environment.properties created with Browser: {}, Env: {}", browser, env);
        } catch (IOException e) {
            logger.warn("[WARNING] Could not create environment.properties: {}", e.getMessage());
        }
    }

    public static void generateAndOpenAllureReport(String resultsPath, String reportPath) {
        logger.info("Generating Allure Report...");
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            
            String command = isWindows ? 
                "cmd.exe /c allure generate " + resultsPath + " -o " + reportPath + " --clean" :
                "allure generate " + resultsPath + " -o " + reportPath + " --clean";
            
            Process process = Runtime.getRuntime().exec(command);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("Allure CLI: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("[SUCCESS] Allure Report generated successfully at: {}", reportPath);
                
                // Automatically open report (optional)
                boolean openReport = Boolean.parseBoolean(System.getProperty("OpenReport", "true"));
                if (openReport) {
                    openAllureReport(reportPath);
                }
            } else {
                logger.error("[ERROR] Allure Report generation failed.");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("[ERROR] Error: {}", e.getMessage());
        }
    }

    private static void openAllureReport(String reportPath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
                
            if (isWindows) {
                Runtime.getRuntime().exec("cmd.exe /c allure open " + reportPath);
                logger.info("[INFO] Opening Allure Report in browser...");
                logger.info("[INFO] Note: The report server is running. You can close it with Ctrl+C.");
            } else {
                logger.info("[INFO] Skipping 'allure open' for OS: {}. Use -DOpenReport=true to override.", os);
            }
        } catch (IOException e) {
            logger.error("[ERROR] Error opening report: {}", e.getMessage());
        }
    }

    public static int getThreadCount() {
        // Priority 1: ThreadCount
        String threadCount = System.getProperty("ThreadCount");
        if (threadCount != null && !threadCount.trim().isEmpty()) {
            try {
                int val = Integer.parseInt(threadCount.trim());
                if (val > 0) {
                    logger.info("[PARALLEL] Using {} threads (from ThreadCount)", val);
                    return val;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        // Priority 2: parallel.threads
        String parallelThreads = System.getProperty("parallel.threads");
        if (parallelThreads != null && !parallelThreads.trim().isEmpty()) {
            try {
                int val = Integer.parseInt(parallelThreads.trim());
                if (val > 0) {
                    logger.info("[PARALLEL] Using {} threads (from parallel.threads)", val);
                    return val;
                }
            } catch (NumberFormatException ignored) {}
        }
        
        // Priority 3: thread.count
        String sysProp = System.getProperty("thread.count");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            try {
                int val = Integer.parseInt(sysProp.trim());
                if (val > 0) return val;
            } catch (NumberFormatException ignored) {}
        }
        
        logger.info("[PARALLEL] No thread count specified, using default: 1");
        return 1;
    }
}
