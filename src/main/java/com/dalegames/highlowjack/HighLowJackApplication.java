package com.dalegames.highlowjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Main Spring Boot application for High Low Jack card game.
 * 
 * <p>Supports two modes:
 * <ul>
 *   <li>Web mode (default): Starts Spring Boot server on port 8089 and opens browser</li>
 *   <li>CLI mode: Runs command-line interface (use --cli flag)</li>
 * </ul>
 *
 * @author Dale &amp; Primus
 * @version 2.0 - Added CLI mode support and browser auto-open
 */
@SpringBootApplication(scanBasePackages = "com.dalegames.highlowjack")
public class HighLowJackApplication {

    /**
     * Main entry point.
     * 
     * <p>Usage:
     * <pre>
     *   java -jar high-low-jack.jar           # Web mode (default)
     *   java -jar high-low-jack.jar --cli     # CLI mode
     * </pre>
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Check for CLI mode flag
        if (args.length > 0 && (args[0].equals("--cli") || args[0].equals("-cli"))) {
            System.out.println("🃏 Starting High Low Jack in CLI mode...\n");
            HighLowJackCLI.main(args);
        } else {
            // Web mode (default)
            System.out.println("🃏 Starting High Low Jack Web Server...");
            System.out.println("📍 Server will start on: http://localhost:8089");
            System.out.println("🎮 Game URL: http://localhost:8089/highlowjack");
            System.out.println("\n💡 Tip: Use --cli flag to run in command-line mode");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Start Spring Boot application
            SpringApplication app = new SpringApplication(HighLowJackApplication.class);
            app.run(args);
            
            // Wait a moment for server to start, then open browser
            openBrowserDelayed();
        }
    }
    
    /**
     * Opens the default web browser to the game URL after a short delay.
     * Uses a separate thread to avoid blocking application startup.
     */
    private static void openBrowserDelayed() {
        new Thread(() -> {
            try {
                // Wait 3 seconds for server to fully start
                Thread.sleep(3000);
                
                String url = "http://localhost:8089/highlowjack";
                
                // Try to open browser
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        System.out.println("🌐 Opening browser to: " + url);
                        desktop.browse(new URI(url));
                        return;
                    }
                }
                
                // If Desktop API didn't work, try OS-specific commands
                String os = System.getProperty("os.name").toLowerCase();
                Runtime runtime = Runtime.getRuntime();
                
                if (os.contains("win")) {
                    // Windows
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    // macOS
                    runtime.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    // Linux
                    runtime.exec("xdg-open " + url);
                } else {
                    System.out.println("⚠️  Could not auto-open browser. Please visit: " + url);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException | java.net.URISyntaxException e) {
                System.out.println("⚠️  Could not auto-open browser. Please visit: http://localhost:8089/highlowjack");
            }
        }).start();
    }
}
