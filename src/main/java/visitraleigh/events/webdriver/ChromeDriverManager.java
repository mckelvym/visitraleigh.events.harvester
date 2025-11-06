package visitraleigh.events.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chrome WebDriver implementation with headless configuration.
 *
 * <p>This class manages the lifecycle of a headless Chrome WebDriver instance
 * configured for web scraping. The driver is created with optimal settings for
 * server environments including headless mode, no sandbox, and custom user agent.
 *
 * <p>Usage:
 * <pre>{@code
 * WebDriverManager manager = new ChromeDriverManager(userAgent, windowSize);
 * WebDriver driver = manager.getDriver();
 * // ... use driver for scraping
 * manager.quit(); // Important: always call quit() to release resources
 * }</pre>
 */
public class ChromeDriverManager implements WebDriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChromeDriverManager.class);

    private WebDriver driver;
    private final String userAgent;
    private final String windowSize;

    /**
     * Creates a new Chrome WebDriver manager with custom configuration.
     *
     * @param userAgent The user agent string to use for the browser
     * @param windowSize The window size as "width,height" (e.g., "1920,1080")
     */
    public ChromeDriverManager(String userAgent, String windowSize) {
        this.userAgent = userAgent;
        this.windowSize = windowSize;
    }

    @Override
    public WebDriver getDriver() {
        if (driver == null) {
            LOG.info("Initializing Chrome WebDriver in headless mode");
            ChromeOptions options = new ChromeOptions();

            // Headless mode configuration
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");

            // Security and stability options
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            // Browser configuration
            options.addArguments("--window-size=" + windowSize);
            options.addArguments("--user-agent=" + userAgent);

            driver = new ChromeDriver(options);
            LOG.info("Chrome WebDriver initialized successfully");
        }
        return driver;
    }

    @Override
    public void quit() {
        if (driver != null) {
            LOG.info("Shutting down Chrome WebDriver");
            driver.quit();
            driver = null;
        }
    }
}
