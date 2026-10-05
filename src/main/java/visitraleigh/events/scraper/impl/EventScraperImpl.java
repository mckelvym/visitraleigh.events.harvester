package visitraleigh.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.parser.EventParser;
import visitraleigh.events.scraper.EventScraper;
import visitraleigh.events.webdriver.PageLoader;

/**
 * Scrapes events using paginated link discovery.
 * Implements the unified 3-phase flow: discover, filter, parse.
 */
public record EventScraperImpl(ScraperConfiguration config,
                               PageLoader pageLoader,
                               EventParser eventParser,
                               PaginationParser paginationParser,
                               EventLinkDiscoverer linkDiscoverer) implements EventScraper {

    private static final Logger LOG = LoggerFactory.getLogger(EventScraperImpl.class);
    private static final DateTimeFormatter PAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM"
        + "/dd/yyyy");

    public EventScraperImpl {
        requireNonNull(config, "config must not be null");
        requireNonNull(pageLoader, "pageLoader must not be null");
        requireNonNull(eventParser, "eventParser must not be null");
        requireNonNull(paginationParser, "paginationParser must not be null");
        requireNonNull(linkDiscoverer, "linkDiscoverer must not be null");
    }

    /**
     * Adds discovered event links keyed by URL, keeping the first occurrence.
     *
     * @param allEventLinks accumulated event links keyed by URL
     * @param eventLinks    event links discovered on one page
     */
    private void addEventLinks(final Map<String, Element> allEventLinks,
                               final List<Element> eventLinks) {
        for (final Element link : eventLinks) {
            allEventLinks.putIfAbsent(link.attr(ABS_HREF_ATTR), link);
        }
    }

    /**
     * Builds the URL for a specific page with date filter.
     *
     * <p>Format: baseUrl?page={page}&endDate={MM/dd/yyyy}
     *
     * @param page    The page number (1-based)
     * @param endDate The end date for filtering events
     * @return The complete URL with pagination and date parameters
     */
    private String buildPageUrl(final int page, final LocalDate endDate) {
        requireNonNull(endDate);
        final String formattedDate = endDate.format(PAGE_DATE_FORMATTER);
        return "%s?page=%d&endDate=%s".formatted(config.getBaseUrl(), page, formattedDate);
    }

    /**
     * Phase 1: Discovers all event link elements from paginated listing pages.
     *
     * <p>This method:
     * <ul>
     *   <li>Loads the first page to determine total page count</li>
     *   <li>Iterates through all pages with date filtering</li>
     *   <li>Discovers event links on each page using EventLinkDiscoverer</li>
     *   <li>Collects all discovered links into a single list</li>
     * </ul>
     *
     * <p>The date filtering is applied via URL parameters to ensure only
     * events within the configured date range are included.
     *
     * @return List of all discovered event link Elements
     */
    private List<Element> discoverEventUrls() {
        final LocalDate endDate = config.getEndDate();

        // Load first page to determine total page count
        final String firstPageUrl = buildPageUrl(1, endDate);
        LOG.info("Loading first page to determine pagination: {}", firstPageUrl);
        final Document firstDoc = pageLoader.loadPage(firstPageUrl,
            config.getLastPageLinkSelector());

        final int totalPages = paginationParser.getNumPages(firstDoc);
        LOG.info("Discovered {} total pages to scrape", totalPages);

        // Keyed by URL (the GUID): recurring events are listed once per occurrence date,
        // so keep only the first (earliest) listing
        final Map<String, Element> allEventLinks = new LinkedHashMap<>();
        addEventLinks(allEventLinks, linkDiscoverer.discoverEventLinks(firstDoc));

        // Iterate through remaining pages
        for (int page = 2; page <= totalPages; page++) {
            final String url = buildPageUrl(page, endDate);
            LOG.info("Scraping page {}/{}: {}", page, totalPages, url);

            final Document doc = pageLoader.loadPage(url, config.getLastPageLinkSelector());
            addEventLinks(allEventLinks, linkDiscoverer.discoverEventLinks(doc));

            LOG.info("Progress: Discovered {} total links so far ({} pages processed)",
                allEventLinks.size(), page);
        }

        return new ArrayList<>(allEventLinks.values());
    }

    /**
     * Phase 2: Filters event links to find only new events.
     *
     * <p>This method extracts the event ID from each link's URL and checks
     * if it exists in the existingGuids set. Only links with new IDs are returned.
     *
     * @param allEventLinks All discovered event link elements
     * @param existingGuids Set of GUIDs for events already in the feed
     * @return List of event link elements for new events only
     */
    private List<Element> filterNewUrls(final List<Element> allEventLinks,
                                        final Set<String> existingGuids) {
        return allEventLinks.stream()
            .filter(link -> {
                final String url = link.attr(ABS_HREF_ATTR);
                final boolean isNew = !existingGuids.contains(url);

                if (!isNew) {
                    LOG.debug("Filtering out existing event: {}", url);
                }

                return isNew;
            })
            .toList();
    }

    /**
     * Parses a single event link and adds it to the events list if successful.
     *
     * <p>This helper method:
     * <ul>
     *   <li>Delegates parsing to EventParser</li>
     *   <li>Logs success with event details</li>
     *   <li>Logs warnings for parsing failures</li>
     *   <li>Adds successfully parsed events to the list</li>
     * </ul>
     *
     * @param link         The event link element to parse
     * @param events       The list to add parsed events to
     * @param currentEvent Current event number (1-based)
     * @param totalEvents  Total number of events being parsed
     */
    private void parseAndAddEvent(final Element link,
                                  final List<EventItem> events,
                                  final int currentEvent,
                                  final int totalEvents) {
        final Optional<EventItem> eventOpt = eventParser.parseEvent(link);

        if (eventOpt.isPresent()) {
            final EventItem event = eventOpt.get();
            events.add(event);
            LOG.info("Parsed event {}/{}: {} ({})",
                currentEvent, totalEvents, event.title(), event.eventDateStart());
        } else {
            LOG.warn("Failed to parse event {}/{}", currentEvent, totalEvents);
        }
    }

    /**
     * Phase 3: Parses event link elements into EventItem objects.
     *
     * <p>This method iterates through filtered event links and:
     * <ul>
     *   <li>Delegates parsing to EventParser</li>
     *   <li>Logs progress and any parsing failures</li>
     *   <li>Collects successfully parsed events</li>
     * </ul>
     *
     * @param newEventLinks Filtered list of new event link elements
     * @return List of successfully parsed EventItem objects
     */
    private List<EventItem> parseEvents(final List<Element> newEventLinks) {
        final List<EventItem> events = new ArrayList<>();
        final int totalEvents = newEventLinks.size();
        int currentEvent = 0;

        for (final Element link : newEventLinks) {
            currentEvent++;
            parseAndAddEvent(link, events, currentEvent, totalEvents);
        }

        return events;
    }

    @Override
    public List<EventItem> scrapeEvents(final Set<String> existingGuids) {
        requireNonNull(existingGuids, "existingGuids must not be null");

        try {
            // Phase 1: Discover event URLs from all pages
            final List<Element> allEventLinks = discoverEventUrls();
            LOG.info("Phase 1 complete: Discovered {} event links", allEventLinks.size());

            // Phase 2: Filter to find new URLs
            final List<Element> newEventLinks = filterNewUrls(allEventLinks, existingGuids);
            LOG.info("Phase 2 complete: {} new events after filtering", newEventLinks.size());

            // Phase 3: Parse events from new URLs
            final List<EventItem> events = parseEvents(newEventLinks);
            LOG.info("Phase 3 complete: Parsed {} events", events.size());

            return events;
        } catch (Exception e) {
            LOG.error("Unable to parse events", e);
            return List.of();
        }
    }
}
