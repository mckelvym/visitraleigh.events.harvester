package visitraleigh.events.scraper;

import java.util.List;
import visitraleigh.events.domain.EventItem;

/**
 * Interface for scraping events from a website.
 *
 * <p>This interface defines the contract for event scraping operations.
 * Implementations are responsible for navigating web pages, discovering
 * event links, and parsing event details.
 *
 * <p>This interface follows the Dependency Inversion Principle and allows
 * for multiple implementations for different event websites.
 */
public interface EventScraper {

    /**
     * Scrapes events from the configured website.
     *
     * <p>This method coordinates the entire scraping process:
     * <ul>
     *   <li>Loading and navigating through paginated event listings</li>
     *   <li>Discovering event links on each page</li>
     *   <li>Parsing details for each event</li>
     *   <li>Filtering out duplicates based on existing GUIDs</li>
     * </ul>
     *
     * @param existingGuids Set of GUIDs for events that already exist in the feed
     * @return List of newly discovered EventItem objects
     * @throws Exception if scraping fails due to network, parsing, or other errors
     */
    List<EventItem> scrapeEvents(java.util.Set<String> existingGuids) throws Exception;

    /**
     * Closes and cleans up any resources used by the scraper.
     *
     * <p>This should be called when scraping is complete to release
     * resources such as WebDriver instances.
     */
    void close();
}
