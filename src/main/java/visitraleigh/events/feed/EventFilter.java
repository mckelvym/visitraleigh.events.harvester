package visitraleigh.events.feed;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.feed.RssElementNames.EV_ENDDATE;
import static visitraleigh.events.feed.RssElementNames.EV_STARTDATE;
import static visitraleigh.events.feed.RssElementNames.PUB_DATE;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.domain.EventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Applies the retention policy: an event is kept until {@code retentionDays} after it ends.
 *
 * <p>The event date is the end date when present, otherwise the start date. Feed items
 * carry these as RSS Event module elements ({@code ev:enddate}, {@code ev:startdate});
 * items written before those elements existed fall back to {@code pubDate}. Items with
 * no parseable date are kept.
 */
public final class EventFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EventFilter.class);
    private final int retentionDays;

    /**
     * Creates a new EventFilter.
     *
     * @param config scraper configuration providing the retention period
     * @throws NullPointerException if config is null
     */
    public EventFilter(final ScraperConfiguration config) {
        requireNonNull(config, "config must not be null");
        this.retentionDays = config.getRetentionDays();
    }

    private static Optional<String> childText(final Element item, final String tagName) {
        final NodeList nodes = item.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return Optional.empty();
        }
        final String text = nodes.item(0).getTextContent().trim();
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }

    private static Optional<LocalDate> extractItemDate(final Element item) {
        return parseDate(item, EV_ENDDATE, DateTimeFormatter.ISO_LOCAL_DATE)
            .or(() -> parseDate(item, EV_STARTDATE, DateTimeFormatter.ISO_LOCAL_DATE))
            .or(() -> parseDate(item, PUB_DATE, DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    private static Optional<LocalDate> parseDate(final Element item, final String tagName,
                                                 final DateTimeFormatter formatter) {
        return childText(item, tagName).flatMap(text -> {
            try {
                return Optional.of(LocalDate.from(formatter.parse(text)));
            } catch (final DateTimeException e) {
                LOG.warn("Unparseable {} '{}': {}", tagName, text, e.getMessage());
                return Optional.empty();
            }
        });
    }

    private LocalDate cutoffDate() {
        return LocalDate.now().minusDays(retentionDays);
    }

    /**
     * Determines whether a newly scraped event is within the retention period.
     *
     * @param event the event to check
     * @return true if the event ends (or starts) on or after the cutoff date
     */
    public boolean shouldKeep(final EventItem event) {
        requireNonNull(event, "event must not be null");
        final LocalDate eventDate = event.eventDateEnd() != null
            ? event.eventDateEnd()
            : event.eventDateStart();
        return !eventDate.isBefore(cutoffDate());
    }

    /**
     * Determines whether an item from the existing feed is within the retention period.
     *
     * @param item the RSS item element
     * @return true if the item's date is on or after the cutoff date, or cannot be determined
     */
    public boolean shouldKeep(final Element item) {
        requireNonNull(item, "item must not be null");
        return extractItemDate(item)
            .map(date -> !date.isBefore(cutoffDate()))
            .orElse(true);
    }
}
