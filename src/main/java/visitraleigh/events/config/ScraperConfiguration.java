package visitraleigh.events.config;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Configuration interface for event scrapers.
 *
 * <p>This interface defines all configuration parameters needed for scraping
 * events from a website. Implementations provide site-specific values for
 * URLs, selectors, timeouts, and other scraping parameters.
 *
 * <p>This interface follows the Dependency Inversion Principle, allowing
 * high-level modules to depend on abstractions rather than concrete
 * configurations.
 */
public interface ScraperConfiguration {

    /**
     * Gets the base URL for the events listing page.
     *
     * @return The base URL (e.g., "https://www.visitraleigh.com/events/")
     */
    String getBaseUrl();

    /**
     * Gets whether debug mode is enabled.
     *
     * <p>Debug mode enables verbose logging and saves HTML pages for inspection.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    boolean isDebugMode();

    /**
     * Gets the default number of pages to scrape if pagination detection fails.
     *
     * @return The default number of pages
     */
    int getDefaultNumPages();

    /**
     * Gets the number of days into the future to scrape events.
     *
     * @return The number of days into the future
     */
    int getDaysIntoFuture();

    /**
     * Gets the number of days after which old events should be dropped from the feed.
     *
     * @return The number of days before events are considered too old
     */
    int getDropEventsOlderThanDays();

    /**
     * Gets the CSS selector for the last page link element in pagination.
     *
     * @return The CSS selector for the last page navigation element
     */
    String getLastPageLinkSelector();

    /**
     * Gets the regex pattern for extracting page numbers from URLs.
     *
     * @return The regex pattern for page number extraction
     */
    Pattern getNumPagesPattern();

    /**
     * Gets the regex pattern for validating event URLs.
     *
     * @return The regex pattern for event URL validation
     */
    Pattern getEventUrlPattern();

    /**
     * Gets the timeout duration for page loads.
     *
     * @return The timeout duration for waiting for pages to load
     */
    Duration getPageLoadTimeout();

    /**
     * Gets the user agent string to use for web scraping.
     *
     * @return The user agent string
     */
    String getUserAgent();

    /**
     * Gets the window size for the headless browser.
     *
     * @return The window size as "widthxheight" (e.g., "1920x1080")
     */
    String getWindowSize();
}
