package visitraleigh.events;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.config.impl.ScraperConfigurationImpl;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.feed.RssFeedManager;
import visitraleigh.events.feed.RssFeedManagerImpl;
import visitraleigh.events.parser.EventParser;
import visitraleigh.events.parser.impl.EventParserImpl;
import visitraleigh.events.scraper.EventScraper;
import visitraleigh.events.scraper.impl.EventLinkDiscoverer;
import visitraleigh.events.scraper.impl.EventScraperImpl;
import visitraleigh.events.scraper.impl.PaginationParser;
import visitraleigh.events.webdriver.ChromeDriverManager;
import visitraleigh.events.webdriver.PageLoader;
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

    private static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static EventScraper getEventScraper(WebDriverManager driverManager,
                                                ScraperConfiguration config) {
        PageLoader pageLoader = new PageLoader(driverManager.getDriver(),
            config.getPageLoadTimeout());
        EventParser parser = new EventParserImpl();
        PaginationParser paginationParser = new PaginationParser(config.getLastPageLinkSelector(),
            config.getNumPagesPattern(), config.getDefaultNumPages());
        EventLinkDiscoverer linkDiscoverer = new EventLinkDiscoverer(config.getEventUrlPattern(),
            "visitraleigh.com/event/");

        return new EventScraperImpl(config, pageLoader, parser, paginationParser, linkDiscoverer);
    }

    /**
     * Main entry point for the application.
     *
     * @param args Command line arguments: [0] = RSS file path
     */
    public static void main(String[] args) {
        // Bridge Java Util Logging to SLF4J for Selenium
        configureLogging();

        if (args.length < 1) {
            LOG.error("Usage: EventsHarvesterApplication <rss-file-path>");
            System.exit(1);
        }

        String rssFilePath = args[0];
        LOG.info("Starting Raleigh Events Harvester");
        LOG.info("Output file: {}", rssFilePath);

        // Manual dependency injection - create all components
        ScraperConfiguration config = new ScraperConfigurationImpl();
        RssFeedManager feedManager = new RssFeedManagerImpl(config);

        try (WebDriverManager driverManager = new ChromeDriverManager(config)) {
            EventScraper scraper = getEventScraper(driverManager, config);

            // Execute workflow
            LOG.info("Loading existing feed");
            Set<String> existingGuids = feedManager.loadExistingGuids(rssFilePath);
            LOG.info("Found {} existing events", existingGuids.size());

            LOG.info("Starting event scraping");
            List<EventItem> newEvents = scraper.scrapeEvents(existingGuids);

            LOG.info("Generating RSS feed");
            feedManager.generateFeed(rssFilePath, newEvents, rssFilePath);

            LOG.info("Successfully generated RSS feed with {} new events", newEvents.size());

            LOG.info("Harvesting completed successfully");
        } catch (Exception e) {
            LOG.error("Error generating RSS feed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
