package visitraleigh.events.parser;

import java.util.Optional;
import org.jsoup.nodes.Element;
import visitraleigh.events.domain.EventItem;

/**
 * Interface for parsing event details from HTML elements.
 *
 * <p>This interface defines the contract for extracting event information
 * from HTML. Implementations use various strategies to extract titles,
 * dates, descriptions, and images from web pages.
 *
 * <p>Following the Open/Closed Principle, new parsing strategies can be
 * added by creating new implementations without modifying existing code.
 */
public interface EventParser {

    /**
     * Parses an event from a link element.
     *
     * <p>This method extracts all event details from the HTML structure
     * surrounding an event link. It typically:
     * <ul>
     *   <li>Extracts the event ID from the URL</li>
     *   <li>Finds the event card container</li>
     *   <li>Extracts title, date, description, and image using field extractors</li>
     * </ul>
     *
     * @param linkElement The HTML element containing the event link
     * @return An Optional containing the parsed EventItem, or empty if parsing fails
     */
    Optional<EventItem> parseEvent(Element linkElement);
}
