package visitraleigh.events;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.config.impl.ScraperConfigurationImpl;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.feed.RssFeedManager;
import visitraleigh.events.feed.RssFeedManagerImpl;
import visitraleigh.events.parser.EventParser;
import visitraleigh.events.parser.impl.EventParserImpl;
import visitraleigh.events.scraper.EventScraper;
import visitraleigh.events.scraper.impl.EventScraperImpl;
import visitraleigh.events.webdriver.ChromeDriverManager;
import visitraleigh.events.webdriver.WebDriverManager;

/**
 * Main application class for the Raleigh Events RSS Generator.
 *
 * <p>This class serves as the entry point for the application and orchestrates
 * the entire event harvesting workflow using dependency injection:
 * <ol>
 *   <li>Creates and configures all dependencies (config, driver, parser, scraper, feed)</li>
 *   <li>Loads existing event GUIDs from RSS feed</li>
 *   <li>Scrapes new events from VisitRaleigh.com</li>
 *   <li>Generates updated RSS feed with new and existing events</li>
 * </ol>
 *
 * <p>This design follows the Dependency Inversion Principle - the main application
 * depends on interfaces, not concrete implementations. All dependencies are
 * manually wired using constructor injection.
 *
 * <p>Usage:
 * <pre>{@code
 * java visitraleigh.events.EventsHarvesterApplication <rss-file-path>
 * }</pre>
 */
public final class EventsHarvesterApplication {

    private static final Logger LOG = LoggerFactory.getLogger(EventsHarvesterApplication.class);

    private EventsHarvesterApplication() {
        // utility
    }

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments: [0] = RSS file path
     */
    public static void main(String[] args) {
        // Bridge Java Util Logging to SLF4J for Selenium
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();

        if (args.length < 1) {
            LOG.error("Usage: EventsHarvesterApplication <rss-file-path>");
            System.exit(1);
        }

        String rssFilePath = args[0];
        LOG.info("Starting Raleigh Events Harvester");
        LOG.info("Output file: {}", rssFilePath);

        // Manual dependency injection - create all components
        ScraperConfiguration config = new ScraperConfigurationImpl();
        WebDriverManager driverManager = new ChromeDriverManager(
                config.getUserAgent(),
                config.getWindowSize());
        EventParser parser = new EventParserImpl(config.isDebugMode());
        RssFeedManager feedManager = new RssFeedManagerImpl(
                config.getDropEventsOlderThanDays());

        try (EventScraper scraper = new EventScraperImpl(config, driverManager, parser)) {
            // Execute workflow
            LOG.info("Loading existing feed");
            Set<String> existingGuids = feedManager.loadExistingGuids(rssFilePath);
            LOG.info("Found {} existing events", existingGuids.size());

            LOG.info("Starting event scraping");
            List<EventItem> newEvents = scraper.scrapeEvents(existingGuids);

            LOG.info("Generating RSS feed");
            feedManager.generateFeed(
                    rssFilePath,
                    newEvents,
                    "Visit Raleigh Events",
                    config.getBaseUrl(),
                    "Events from Visit Raleigh");

            LOG.info("Successfully generated RSS feed with {} new events", newEvents.size());

            LOG.info("Harvesting completed successfully");
            System.exit(0);

        } catch (Exception e) {
            LOG.error("Error generating RSS feed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
