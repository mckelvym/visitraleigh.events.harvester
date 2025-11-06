package visitraleigh.events.parser.raleigh;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.parser.EventParser;

/**
 * Event parser implementation for VisitRaleigh.com HTML structure.
 *
 * <p>This class orchestrates the parsing of event information from HTML elements
 * using specialized field extractors for each piece of information (title, date,
 * description, image).
 *
 * <p>The parsing process:
 * <ol>
 *   <li>Extracts event ID from the URL</li>
 *   <li>Finds the event card container in the DOM</li>
 *   <li>Extracts title (required - returns empty if missing)</li>
 *   <li>Extracts date, description, and image (optional)</li>
 *   <li>Builds full title with date if available</li>
 *   <li>Returns EventItem with all extracted information</li>
 * </ol>
 */
public class RaleighEventParser implements EventParser {

    private static final Logger LOG = LoggerFactory.getLogger(RaleighEventParser.class);

    private final EventCardFinder cardFinder;
    private final TitleExtractor titleExtractor;
    private final DateExtractor dateExtractor;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final boolean debugMode;

    /**
     * Creates a new Raleigh event parser with default field extractors.
     *
     * @param debugMode Whether to enable debug logging
     */
    public RaleighEventParser(boolean debugMode) {
        this.cardFinder = new EventCardFinder();
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.descriptionExtractor = new DescriptionExtractor();
        this.imageExtractor = new ImageExtractor();
        this.debugMode = debugMode;
    }

    @Override
    public Optional<EventItem> parseEvent(Element linkElement) {
        try {
            String eventUri = linkElement.attr("abs:href");
            logDebug("Parsing link: {}", eventUri);

            // Extract event ID from URL
            int id = extractEventId(eventUri);

            // Find the event card container
            Element eventCard = cardFinder.findEventCardContainer(linkElement);

            // Extract title (required)
            Optional<String> titleOpt = titleExtractor.extractTitle(eventCard);
            if (titleOpt.isEmpty()) {
                LOG.debug("Skipping event with no valid title: {}", eventUri);
                return Optional.empty();
            }
            String title = titleOpt.get();
            logDebug("Title: {}", title);

            // Extract optional fields
            String dateStr = dateExtractor.extractDate(eventCard);
            String description = descriptionExtractor.extractDescription(eventCard);
            String imageUrl = imageExtractor.extractImageUrl(eventCard);

            // Build full title with date
            String fullTitle = buildFullTitle(title, dateStr);

            // Create event item
            EventItem event = new EventItem(
                    id,
                    eventUri,     // guid
                    fullTitle,
                    description,
                    eventUri,     // link
                    imageUrl,
                    dateStr);

            LOG.debug("Successfully parsed event: {}", fullTitle);
            return Optional.of(event);

        } catch (Exception e) {
            logDebug("Error parsing link: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Extracts the event ID from an event URI.
     *
     * <p>The ID is the last numeric segment in the URL path.
     * Example: /event/music-festival/12345/ → 12345
     *
     * @param eventUri The event URL
     * @return The extracted event ID, or -1 if extraction fails
     */
    private int extractEventId(String eventUri) {
        final List<String> strings = Splitter.on("/")
                .omitEmptyStrings()
                .splitToList(eventUri);
        final String lastElement = strings.get(strings.size() - 1);
        return getEventId(lastElement);
    }

    /**
     * Parses a string as an integer event ID.
     *
     * @param lastElement The string to parse
     * @return The parsed ID, or -1 if parsing fails
     */
    private static int getEventId(String lastElement) {
        try {
            return Integer.parseInt(lastElement);
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse event ID as int: {}", lastElement);
            return -1;
        }
    }

    /**
     * Builds the full title by appending the date if available.
     *
     * @param title The base title
     * @param dateStr The date string
     * @return The full title with date in parentheses, or just the title if no date
     */
    private String buildFullTitle(String title, String dateStr) {
        if (!dateStr.isEmpty()) {
            return title + " (" + dateStr + ")";
        }
        return title;
    }

    /**
     * Logs debug messages if debug mode is enabled.
     *
     * @param message The log message
     * @param args The log arguments
     */
    private void logDebug(String message, Object... args) {
        if (debugMode) {
            LOG.debug(message, args);
        }
    }
}
