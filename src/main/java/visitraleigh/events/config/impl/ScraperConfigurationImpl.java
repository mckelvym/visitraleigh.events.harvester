package visitraleigh.events.config.impl;

import static visitraleigh.events.parser.impl.CssSelectors.LAST_PAGE_LINK_ELEMENT;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.DEFAULT_WINDOW_SIZE;

import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Pattern;
import visitraleigh.events.config.ScraperConfiguration;

/**
 * Configuration implementation for VisitRaleigh.com event scraping.
 *
 * <p>This class provides all configuration parameters specific to scraping
 * events from visitraleigh.com, including URLs, CSS selectors, regex patterns,
 * and environment-based settings.
 *
 * <p>Configuration values can be customized via environment variables:
 * <ul>
 *   <li>DAYS_INTO_FUTURE - Number of days into future to scrape (default: 30)</li>
 *   <li>DROP_EVENTS_OLDER_THAN_DAYS - Age threshold for dropping events (default: 30)</li>
 * </ul>
 */
public class ScraperConfigurationImpl implements ScraperConfiguration {

    // Site-specific constants
    private static final String BASE_URL = "https://www.visitraleigh.com/events/";
    private static final int DEFAULT_NUM_PAGES = 10;
    private static final Pattern EVENT_URL_PATTERN =
            Pattern.compile("/event/[^/]+/\\d+/?$");
    private static final String FEED_DESCRIPTION = "Events from Visit Raleigh";
    // RSS feed metadata
    private static final String FEED_TITLE = "Visit Raleigh Events";
    // Regex patterns
    private static final Pattern NUM_PAGES_PATTERN =
            Pattern.compile("(?:^|[?&])page=(\\d+)");
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);
    // Browser configuration
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";

    // Environment-based configuration
    private final int daysIntoFuture;
    private final int retentionDays;

    /**
     * Creates a new configuration with default values and environment overrides.
     *
     * <p>This constructor reads environment variables to customize the
     * date range for event scraping. If environment variables are not set
     * or are invalid, default values are used.
     */
    public ScraperConfigurationImpl() {
        this.daysIntoFuture = getDaysIntoFutureFromEnv();
        this.retentionDays = getDropEventsOlderThanDaysFromEnv();
    }

    /**
     * Reads DAYS_INTO_FUTURE from environment, with fallback to default.
     *
     * @return The number of days into future to scrape events
     */
    private static int getDaysIntoFutureFromEnv() {
        String envValue = System.getenv("DAYS_INTO_FUTURE");
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return 30;
    }

    /**
     * Reads DROP_EVENTS_OLDER_THAN_DAYS from environment, with fallback to default.
     *
     * @return The number of days before events are considered too old
     */
    private static int getDropEventsOlderThanDaysFromEnv() {
        String envValue = System.getenv("DROP_EVENTS_OLDER_THAN_DAYS");
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return 30;
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public int getDaysIntoFuture() {
        return daysIntoFuture;
    }

    @Override
    public int getDefaultNumPages() {
        return DEFAULT_NUM_PAGES;
    }

    /**
     * Gets the end date for event scraping based on days into future.
     *
     * @return The end date as a LocalDate object
     */
    public LocalDate getEndDate() {
        return LocalDate.now().plusDays(daysIntoFuture);
    }

    @Override
    public Pattern getEventUrlPattern() {
        return EVENT_URL_PATTERN;
    }

    @Override
    public String getFeedDescription() {
        return FEED_DESCRIPTION;
    }

    @Override
    public String getFeedLink() {
        return getBaseUrl();
    }

    @Override
    public String getFeedTitle() {
        return FEED_TITLE;
    }

    @Override
    public String getLastPageLinkSelector() {
        return LAST_PAGE_LINK_ELEMENT;
    }

    @Override
    public Pattern getNumPagesPattern() {
        return NUM_PAGES_PATTERN;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }

    @Override
    public int getRetentionDays() {
        return retentionDays;
    }

    @Override
    public String getUserAgent() {
        return USER_AGENT;
    }

    @Override
    public String getWindowSize() {
        return DEFAULT_WINDOW_SIZE;
    }
}
