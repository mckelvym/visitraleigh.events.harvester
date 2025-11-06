package visitraleigh.events.parser.raleigh;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event dates from HTML elements.
 *
 * <p>This class extracts date information from event cards by looking for:
 * <ul>
 *   <li>HTML5 time elements</li>
 *   <li>Elements with 'date' or 'Date' in class name</li>
 * </ul>
 */
public class DateExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DateExtractor.class);

    /**
     * Extracts the date string from an event card element.
     *
     * @param eventCard The event card container element
     * @return The extracted date string, or empty string if not found
     */
    public String extractDate(Element eventCard) {
        Element dateElement = eventCard.selectFirst("time, [class*='date'], [class*='Date']");

        if (dateElement != null) {
            String dateText = dateElement.text().trim();
            LOG.debug("Extracted date: {}", dateText);
            return dateText;
        }

        LOG.debug("No date found in event card");
        return "";
    }
}
