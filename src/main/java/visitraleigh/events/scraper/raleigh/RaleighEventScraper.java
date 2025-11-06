package visitraleigh.events.scraper.raleigh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.parser.EventParser;
import visitraleigh.events.scraper.EventScraper;
import visitraleigh.events.webdriver.PageLoader;
import visitraleigh.events.webdriver.WebDriverManager;

/**
 * Event scraper implementation for VisitRaleigh.com.
 *
 * <p>This class orchestrates the entire event scraping workflow:
 * <ol>
 *   <li>Initializes WebDriver and PageLoader</li>
 *   <li>Iterates through paginated event listings</li>
 *   <li>Discovers event links on each page</li>
 *   <li>Parses event details for each link</li>
 *   <li>Filters out duplicates based on existing GUIDs</li>
 *   <li>Returns list of new events</li>
 * </ol>
 *
 * <p>This class follows the Template Method pattern, with the main algorithm
 * defined in {@link #scrapeEvents(Set)} and helper methods handling specific steps.
 */
public class RaleighEventScraper implements EventScraper {

    private static final Logger LOG = LoggerFactory.getLogger(RaleighEventScraper.class);

    private final ScraperConfiguration config;
    private final WebDriverManager driverManager;
    private final EventParser parser;
    private final PaginationParser paginationParser;
    private final EventLinkDiscoverer linkDiscoverer;
    private final PageLoader pageLoader;

    /**
     * Creates a new Raleigh event scraper with injected dependencies.
     *
     * @param config The scraper configuration
     * @param driverManager The WebDriver manager
     * @param parser The event parser
     */
    public RaleighEventScraper(
            ScraperConfiguration config,
            WebDriverManager driverManager,
            EventParser parser) {
        this.config = config;
        this.driverManager = driverManager;
        this.parser = parser;

        // Initialize helper components
        this.paginationParser = new PaginationParser(
                config.getLastPageLinkSelector(),
                config.getNumPagesPattern(),
                config.getDefaultNumPages());

        this.linkDiscoverer = new EventLinkDiscoverer(
                config.getEventUrlPattern(),
                "visitraleigh.com/event/");

        WebDriver driver = driverManager.getDriver();
        this.pageLoader = new PageLoader(
                driver,
                config.getPageLoadTimeout(),
                config.getLastPageLinkSelector(),
                config.isDebugMode());
    }

    @Override
    public List<EventItem> scrapeEvents(Set<String> existingGuids) throws Exception {
        LOG.info("Starting event scraping from {}", config.getBaseUrl());

        List<EventItem> newEvents = new ArrayList<>();

        try {
            int numPages = scrapeAllPages(existingGuids, newEvents);
            LOG.info("Successfully scraped {} new events across {} pages",
                    newEvents.size(), numPages);
        } catch (Exception e) {
            LOG.error("Error during scraping: {}", e.getMessage(), e);
            throw e;
        }

        return newEvents;
    }

    @Override
    public void close() {
        driverManager.quit();
    }

    /**
     * Scrapes all pages of event listings.
     *
     * @param existingGuids Set of GUIDs for events already in the feed
     * @param newEvents List to collect newly discovered events
     * @return The total number of pages scraped
     * @throws IOException if page loading fails
     */
    private int scrapeAllPages(Set<String> existingGuids, List<EventItem> newEvents)
            throws IOException {
        int page = 1;
        int numPages = 1;
        String endDate = getEndDate();

        while (page <= numPages) {
            String url = buildPageUrl(page, endDate);
            LOG.info("Scraping page {}: {}", page, url);

            Document doc = pageLoader.loadAndParse(url, page);

            // On first page, determine total number of pages
            if (page == 1) {
                numPages = paginationParser.getNumPages(doc);
                LOG.info("Found {} pages to scrape", numPages);
            }

            // Scrape events from current page
            scrapeEventsFromPage(doc, existingGuids, newEvents);
            LOG.info("Scraped {} new events so far (page {} of {})",
                    newEvents.size(), page, numPages);

            page++;
        }

        return numPages;
    }

    /**
     * Scrapes events from a single page.
     *
     * @param doc The parsed HTML document
     * @param existingGuids Set of GUIDs for events already in the feed
     * @param newEvents List to collect newly discovered events
     */
    private void scrapeEventsFromPage(
            Document doc,
            Set<String> existingGuids,
            List<EventItem> newEvents) {

        // Discover event links on the page
        List<Element> eventLinks = linkDiscoverer.discoverEventLinks(doc);

        // Parse each event link
        for (Element link : eventLinks) {
            Optional<EventItem> eventOpt = parser.parseEvent(link);

            if (eventOpt.isPresent()) {
                EventItem event = eventOpt.get();

                // Check if event already exists
                if (!existingGuids.add(event.guid())) {
                    LOG.info("Found existing event: {}", event.title());
                } else {
                    newEvents.add(event);
                    LOG.info("Found new event: {}", event.title());
                }
            }
        }
    }

    /**
     * Builds the URL for a specific page with date filter.
     *
     * @param page The page number
     * @param endDate The end date for filtering
     * @return The complete URL
     */
    private String buildPageUrl(int page, String endDate) {
        return config.getBaseUrl() + "?page=" + page + "&endDate=" + endDate;
    }

    /**
     * Gets the end date for event scraping based on configuration.
     *
     * @return The end date formatted as MM/dd/yyyy
     */
    private String getEndDate() {
        if (config instanceof visitraleigh.events.config.raleigh.RaleighScraperConfiguration) {
            return ((visitraleigh.events.config.raleigh.RaleighScraperConfiguration) config)
                    .getEndDate();
        }
        // Fallback: calculate end date from current date + days into future
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return java.time.ZonedDateTime.now()
                .plusDays(config.getDaysIntoFuture())
                .format(formatter);
    }
}
