package visitraleigh.events.feed;

import java.util.List;
import java.util.Set;
import visitraleigh.events.domain.EventItem;

/**
 * Interface for managing RSS feed operations.
 *
 * <p>This interface defines operations for reading existing RSS feeds,
 * writing new feeds, and managing event items within feeds.
 *
 * <p>Implementations handle XML parsing, security (XXE protection),
 * event filtering, and feed generation according to RSS 2.0 specification.
 */
public interface RssFeedManager {

    /**
     * Generates and writes an RSS feed with new and existing events.
     *
     * <p>This method:
     * <ul>
     *   <li>Creates an RSS 2.0 XML document</li>
     *   <li>Adds new events (sorted by ID, descending)</li>
     *   <li>Imports existing events from the old feed</li>
     *   <li>Filters out events older than the configured threshold</li>
     *   <li>Writes the result to the specified file</li>
     * </ul>
     *
     * <p>Channel metadata (title, link, description) are retrieved from
     * the ScraperConfiguration.
     *
     * @param filePath         The path where the RSS file should be written
     * @param newEvents        List of new events to add to the feed
     * @param existingFilePath The path of the existing feed to merge
     * @throws Exception if generation or writing fails
     */
    void generateFeed(String filePath, List<EventItem> newEvents, String existingFilePath)
        throws Exception;

    /**
     * Loads existing event GUIDs from an RSS feed file.
     *
     * <p>This method reads an existing RSS file and extracts all GUIDs
     * to prevent duplicate events in the feed.
     *
     * @param filePath The path to the RSS file
     * @return Set of GUIDs from existing events
     * @throws Exception if reading or parsing the feed fails
     */
    Set<String> loadExistingGuids(String filePath)
        throws Exception;
}
