package visitraleigh.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.CssSelectors.DATE_CLASS;
import static visitraleigh.events.parser.impl.CssSelectors.DATE_CLASS_CAPITALIZED;
import static visitraleigh.events.parser.impl.CssSelectors.TIME_ELEMENT;
import static visitraleigh.events.parser.impl.HtmlConstants.EMPTY;

import org.jsoup.nodes.Element;

/**
 * Extracts event dates from HTML elements.
 *
 * <p>This class extracts date information from event cards by looking for:
 * <ul>
 *   <li>HTML5 time elements</li>
 *   <li>Elements with 'date' or 'Date' in class name</li>
 * </ul>
 */
public final class DateExtractor {

    /**
     * Extracts the date string from an event card element.
     *
     * @param eventElement The event card container element
     * @return The extracted date string, or empty string if not found
     * @throws NullPointerException if eventCard is null
     */
    public String extractDateString(Element eventElement) {
        requireNonNull(eventElement, "eventCard must not be null");
        Element dateElement = eventElement.selectFirst(
            "%s, %s, %s".formatted(TIME_ELEMENT, DATE_CLASS, DATE_CLASS_CAPITALIZED));

        if (dateElement == null) {
            return EMPTY;
        }

        return dateElement.text().trim();
    }
}
