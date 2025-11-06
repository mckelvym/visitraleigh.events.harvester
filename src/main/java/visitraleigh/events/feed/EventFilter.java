package visitraleigh.events.feed;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Filters events by age and provides statistics.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Filtering out events older than a configured threshold</li>
 *   <li>Extracting publication dates from RSS items</li>
 *   <li>Calculating event age statistics</li>
 * </ul>
 */
public class EventFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EventFilter.class);
    private static final DateTimeFormatter RFC_1123_FORMATTER =
            DateTimeFormatter.RFC_1123_DATE_TIME;

    private final int dropEventsOlderThanDays;

    /**
     * Creates a new event filter with the specified age threshold.
     *
     * @param dropEventsOlderThanDays The number of days - events older than
     *                                this are filtered out
     */
    public EventFilter(int dropEventsOlderThanDays) {
        this.dropEventsOlderThanDays = dropEventsOlderThanDays;
    }

    /**
     * Determines whether an event should be kept based on its publication date.
     *
     * @param item The RSS item node
     * @return true if the event should be kept, false if it should be dropped
     */
    public boolean shouldKeepEvent(Node item) {
        ZonedDateTime pubDate = extractPubDateFromItem(item);

        if (pubDate == null) {
            // If no date, keep the event
            return true;
        }

        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(dropEventsOlderThanDays);
        return pubDate.isAfter(cutoffDate);
    }

    /**
     * Extracts the publication date from an RSS item.
     *
     * @param item The RSS item node
     * @return The parsed publication date, or null if not found or invalid
     */
    public ZonedDateTime extractPubDateFromItem(Node item) {
        NodeList children = item.getChildNodes();

        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("pubDate".equals(child.getNodeName())) {
                try {
                    String pubDateStr = child.getTextContent();
                    return ZonedDateTime.parse(pubDateStr, RFC_1123_FORMATTER);
                } catch (Exception e) {
                    LOG.debug("Failed to parse pubDate: {}", child.getTextContent(), e);
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Finds the publication date of the last (oldest) event in a list.
     *
     * @param items NodeList of RSS item elements
     * @return Optional containing the oldest date, or empty if no items or no date
     */
    public Optional<ZonedDateTime> findLastPubDate(NodeList items) {
        if (items.getLength() <= 0) {
            return Optional.empty();
        }

        Node item = items.item(items.getLength() - 1);
        return Optional.ofNullable(extractPubDateFromItem(item));
    }

    /**
     * Logs statistics about the RSS feed.
     *
     * @param totalEvents The total number of events in the feed
     * @param droppedEventsCount The number of events that were dropped
     * @param oldestDate The publication date of the oldest event
     */
    public void logFeedStatistics(int totalEvents, int droppedEventsCount,
                                   ZonedDateTime oldestDate) {
        if (totalEvents == 0) {
            LOG.info("RSS feed contains 0 events");
            if (droppedEventsCount > 0) {
                LOG.info("Dropped {} old events (older than {} days)",
                        droppedEventsCount, dropEventsOlderThanDays);
            }
            return;
        }

        if (oldestDate != null) {
            long daysSinceOldest = Duration.between(oldestDate, ZonedDateTime.now()).toDays();
            LOG.info("RSS feed contains {} total events, oldest entry is {} days old",
                    totalEvents, daysSinceOldest);
        } else {
            LOG.info("RSS feed contains {} total events", totalEvents);
        }

        if (droppedEventsCount > 0) {
            LOG.info("Dropped {} old events (older than {} days)",
                    droppedEventsCount, dropEventsOlderThanDays);
        }
    }
}
