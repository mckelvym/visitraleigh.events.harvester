package visitraleigh.events.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Interface for managing WebDriver instances.
 *
 * <p>This interface abstracts WebDriver lifecycle management
 *
 * <p>Following the Dependency Inversion Principle, scrapers depend on
 * this interface rather than concrete WebDriver implementations.
 *
 * <p>Implements AutoCloseable to support try-with-resources pattern
 * for proper resource management.
 */
public interface WebDriverManager extends AutoCloseable {

    /**
     * Closes the WebDriver (delegates to quit()).
     *
     * <p>Required by AutoCloseable. Implementations should delegate
     * to quit() for actual cleanup.
     */
    @Override
    default void close() {
        quit();
    }

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
