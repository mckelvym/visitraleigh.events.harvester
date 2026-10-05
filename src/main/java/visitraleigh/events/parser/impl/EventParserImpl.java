package visitraleigh.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;

import com.google.common.base.Splitter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.domain.EventItem;
import visitraleigh.events.parser.EventParser;

/**
 * This class orchestrates the parsing of event information from HTML elements
 * using specialized field extractors for each piece of information (title, date,
 * description, image).
 */
public final class EventParserImpl implements EventParser {

    private static final String DATES_VARY_BETWEEN = "Dates vary between ";
    private static final Logger LOG = LoggerFactory.getLogger(EventParserImpl.class);
    private static final String RECURRING = "Recurring ";
    private static final String UNTIL = " until ";

    private final EventCardFinder cardFinder;
    private final DateExtractor dateExtractor;
    private final DateParser dateParser;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Creates a new Raleigh event parser with default field extractors.
     */
    public EventParserImpl() {
        this.cardFinder = new EventCardFinder();
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.dateParser = new DateParser();
        this.descriptionExtractor = new DescriptionExtractor();
        this.imageExtractor = new ImageExtractor();
    }

    /**
     * Builds the full title by appending the date if available.
     *
     * @param title   The base title
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
     * Extracts the event ID from an event URI as a String.
     *
     * <p>The ID is the last segment in the URL path.
     * Example: /event/music-festival/12345/ → "12345"
     *
     * @param eventUri The event URL
     * @return The extracted event ID as a String
     */
    private String extractEventIdAsString(String eventUri) {
        final List<String> strings = Splitter.on("/")
            .omitEmptyStrings()
            .splitToList(eventUri);
        return strings.get(strings.size() - 1);
    }

    /**
     * Parses a date string that may contain a single date or a date range.
     * Handles multiple formats including:
     * - "January 15 - February 20, 2024" (standard date range)
     * - "Dates vary between January 3, 2026 - January 4, 2026" (prefixed range)
     * - "Recurring weekly on Sunday until December 27, 2026" (recurring with end)
     * - "Recurring weekly on Sunday" (recurring without end)
     * - "Recurring monthly on the 1st Sunday" (recurring monthly)
     *
     * @param dateStr The date string to parse
     * @return A DateRange with start (and optionally end) dates
     */
    private DateRange parseDateRange(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            // Use today's date as fallback
            return new DateRange(LocalDate.now(), null);
        }

        String processedStr = dateStr;

        // Handle "Dates vary between X - Y" pattern
        if (processedStr.startsWith(DATES_VARY_BETWEEN)) {
            processedStr = processedStr.substring(DATES_VARY_BETWEEN.length());
        }

        // Handle "Recurring" patterns
        if (processedStr.startsWith(RECURRING)) {
            return parseRecurringDate(processedStr);
        }

        // Check for date range (contains " - " or " – " or " to ")
        String[] rangeSeparators = {" - ", " – ", " to "};
        for (String separator : rangeSeparators) {
            if (processedStr.contains(separator)) {
                String[] parts = processedStr.split(separator);
                if (parts.length == 2) {
                    LocalDate startDate = parseSingleDate(parts[0].trim(), dateStr);
                    LocalDate endDate = parseSingleDate(parts[1].trim(), dateStr);
                    if (startDate != null) {
                        return new DateRange(startDate, endDate);
                    }
                }
            }
        }

        // Single date
        LocalDate date = parseSingleDate(processedStr, dateStr);
        return new DateRange(date, null);
    }

    @Override
    public Optional<EventItem> parseEvent(Element eventElement) {
        requireNonNull(eventElement, "eventElement must not be null");
        try {
            String eventUri = eventElement.attr(ABS_HREF_ATTR);

            // Extract event ID from URL as String
            String id = extractEventIdAsString(eventUri);

            // Find the event card container
            Element eventCard = cardFinder.findEventCardContainer(eventElement);

            // Extract title (required)
            Optional<String> titleOpt = titleExtractor.extractTitle(eventCard);
            if (titleOpt.isEmpty()) {
                return Optional.empty();
            }
            String title = titleOpt.get();

            // Extract optional fields
            String dateStr = dateExtractor.extractDateString(eventCard);
            String description = descriptionExtractor.extract(eventCard);
            String imageUrl = imageExtractor.extractImageUrl(eventCard);

            // Parse date(s) from date string
            DateRange dateRange = parseDateRange(dateStr);
            if (dateRange.start() == null) {
                return Optional.empty();
            }

            // Build full title with date
            String fullTitle = buildFullTitle(title, dateStr);

            // Create event item with new standard format
            EventItem event = new EventItem(
                id,
                fullTitle,
                eventUri,     // link
                description,
                dateRange.start(),
                dateRange.end(),
                imageUrl,
                null);        // location

            return Optional.of(event);

        } catch (Exception e) {
            LOG.warn("Error parsing link: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Parses recurring event date patterns.
     * Examples:
     * - "Recurring weekly on Sunday until December 27, 2026"
     * - "Recurring weekly on Sunday"
     * - "Recurring monthly on the 1st Sunday"
     *
     * @param dateStr The recurring date string
     * @return A DateRange with start as today and optional end date
     */
    private DateRange parseRecurringDate(String dateStr) {
        // For recurring events, use today as the start date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = null;

        // Check if there's an "until [date]" clause
        int untilIndex = dateStr.indexOf(UNTIL);
        if (untilIndex != -1) {
            String endDateStr = dateStr.substring(untilIndex + UNTIL.length()).trim();
            endDate = dateParser.parse(endDateStr);
            if (endDate == null) {
                LOG.debug("Could not parse end date from recurring pattern: {}", endDateStr);
            }
        }

        return new DateRange(startDate, endDate);
    }

    /**
     * Attempts to parse a single date string using the centralized DateParser.
     *
     * @param dateStr     The date string to parse
     * @param originalStr The original full date string for logging
     * @return The parsed LocalDate, or null if parsing fails
     */
    private LocalDate parseSingleDate(String dateStr, String originalStr) {
        LocalDate result = dateParser.parse(dateStr);
        if (result == null) {
            LOG.warn("Unable to parse date: {}", originalStr);
        }
        return result;
    }

    /**
     * Simple record to hold parsed date range.
     */
    private record DateRange(LocalDate start, LocalDate end) {
    }
}
