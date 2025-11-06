package visitraleigh.events.config.raleigh;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RaleighScraperConfiguration implements ScraperConfiguration {

    private static final Logger LOG =
            LoggerFactory.getLogger(RaleighScraperConfiguration.class);

    // Site-specific constants
    private static final String BASE_URL = "https://www.visitraleigh.com/events/";
    private static final boolean DEBUG_MODE = false;
    private static final int DEFAULT_NUM_PAGES = 10;
    private static final String LAST_PAGE_LINK_ELEMENT = "li.arrow.arrow-next.arrow-double";
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);

    // Regex patterns
    private static final Pattern NUM_PAGES_PATTERN =
            Pattern.compile("(?:^|[?&])page=(\\d+)");
    private static final Pattern EVENT_URL_PATTERN =
            Pattern.compile("/event/[^/]+/\\d+/?$");

    // Browser configuration
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";
    private static final String WINDOW_SIZE = "1920,1080";

    // Environment-based configuration
    private final int daysIntoFuture;
    private final int dropEventsOlderThanDays;

    /**
     * Creates a new configuration with default values and environment overrides.
     *
     * <p>This constructor reads environment variables to customize the
     * date range for event scraping. If environment variables are not set
     * or are invalid, default values are used.
     */
    public RaleighScraperConfiguration() {
        this.daysIntoFuture = getDaysIntoFutureFromEnv();
        this.dropEventsOlderThanDays = getDropEventsOlderThanDaysFromEnv();
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public boolean isDebugMode() {
        return DEBUG_MODE;
    }

    @Override
    public int getDefaultNumPages() {
        return DEFAULT_NUM_PAGES;
    }

    @Override
    public int getDaysIntoFuture() {
        return daysIntoFuture;
    }

    @Override
    public int getDropEventsOlderThanDays() {
        return dropEventsOlderThanDays;
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
    public Pattern getEventUrlPattern() {
        return EVENT_URL_PATTERN;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }

    @Override
    public String getUserAgent() {
        return USER_AGENT;
    }

    @Override
    public String getWindowSize() {
        return WINDOW_SIZE;
    }

    /**
     * Gets the end date for event scraping based on days into future.
     *
     * @return The end date formatted as MM/dd/yyyy
     */
    public String getEndDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return ZonedDateTime.now().plusDays(daysIntoFuture).format(formatter);
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
                LOG.warn("Invalid DAYS_INTO_FUTURE value: {}, using default of 30",
                        envValue);
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
                LOG.warn(
                        "Invalid DROP_EVENTS_OLDER_THAN_DAYS value: {}, using default of 30",
                        envValue);
            }
        }
        return 30;
    }
}
