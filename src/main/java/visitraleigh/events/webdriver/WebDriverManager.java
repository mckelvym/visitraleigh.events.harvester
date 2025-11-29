package visitraleigh.events.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Interface for managing WebDriver instances.
 *
 * <p>This interface abstracts WebDriver lifecycle management, allowing
 * for different browser implementations (Chrome, Firefox, etc.) and
 * configurations (headless, window size, user agent, etc.).
 *
 * <p>Following the Dependency Inversion Principle, scrapers depend on
 * this interface rather than concrete WebDriver implementations.
 */
public interface WebDriverManager {

    /**
     * Gets a configured WebDriver instance.
     *
     * <p>The WebDriver should be properly configured according to the
     * implementation's requirements (e.g., headless mode, window size,
     * user agent).
     *
     * @return A configured WebDriver instance
     */
    WebDriver getDriver();

    /**
     * Quits the WebDriver and releases resources.
     *
     * <p>This should be called when web scraping is complete to properly
     * shut down the browser and free system resources.
     */
    void quit();
}
