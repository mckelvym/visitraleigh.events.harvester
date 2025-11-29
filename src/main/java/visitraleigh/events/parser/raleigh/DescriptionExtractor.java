package visitraleigh.events.parser.raleigh;

import static visitraleigh.events.parser.raleigh.CssSelectors.BLOCK_META_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.BLOCK_META_DIV;
import static visitraleigh.events.parser.raleigh.CssSelectors.BR_TAG;
import static visitraleigh.events.parser.raleigh.CssSelectors.DATE_INFO_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.DESCRIPTION_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.EXCERPT_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.LOCATION_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.PARAGRAPH;
import static visitraleigh.events.parser.raleigh.CssSelectors.REGION_CLASS;
import static visitraleigh.events.parser.raleigh.CssSelectors.REGION_TEXT;
import static visitraleigh.events.parser.raleigh.CssSelectors.SPACE_SEPARATOR;
import static visitraleigh.events.parser.raleigh.CssSelectors.TIMES_CLASS;

import java.util.function.UnaryOperator;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event descriptions using fallback strategies.
 *
 * <p>This class attempts to extract event descriptions using two strategies:
 * <ol>
 *   <li>From structured block-meta div with date, time, and location info</li>
 *   <li>From generic description elements (p tags, description classes)</li>
 * </ol>
 */
public class DescriptionExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionExtractor.class);

    /**
     * Extracts the description from an event card element.
     *
     * <p>First tries to extract from block-meta div, then falls back to
     * generic description elements.
     *
     * @param eventCard The event card container element
     * @return The extracted description, or empty string if not found
     */
    public String extractDescription(Element eventCard) {
        String description = extractDescriptionFromBlockMeta(eventCard);

        if (description.isEmpty()) {
            description = extractDescriptionFromFallback(eventCard);
        }

        if (!description.isEmpty()) {
            LOG.debug("Extracted description ({} chars)", description.length());
        } else {
            LOG.debug("No description found in event card");
        }

        return description;
    }

    /**
     * Extracts description from structured block-meta div.
     *
     * <p>Looks for a block-meta div and extracts:
     * <ul>
     *   <li>Date info</li>
     *   <li>Times</li>
     *   <li>Location</li>
     *   <li>Region</li>
     * </ul>
     *
     * @param eventCard The event card container
     * @return The extracted description, or empty string if not found
     */
    private String extractDescriptionFromBlockMeta(Element eventCard) {
        Element blockMeta = eventCard.selectFirst(BLOCK_META_DIV + ", " + BLOCK_META_CLASS);
        if (blockMeta == null) {
            return "";
        }

        StringBuilder descBuilder = new StringBuilder();
        UnaryOperator<String> liWrap = str -> BR_TAG + str;

        appendTextIfPresent(descBuilder, blockMeta, DATE_INFO_CLASS, liWrap);
        appendTextIfPresent(descBuilder, blockMeta, TIMES_CLASS, liWrap);
        appendTextIfPresent(descBuilder, blockMeta, LOCATION_CLASS, liWrap);
        appendTextIfPresent(descBuilder, blockMeta, REGION_CLASS, liWrap);

        return descBuilder.toString().trim();
    }

    /**
     * Appends text from an element to the builder if present.
     *
     * @param builder The StringBuilder to append to
     * @param parent The parent element to search in
     * @param selector The CSS selector to find the element
     * @param wrapper A function to wrap the text (e.g., add <br/> tags)
     */
    private void appendTextIfPresent(
            StringBuilder builder,
            Element parent,
            String selector,
            UnaryOperator<String> wrapper) {
        Element element = parent.selectFirst(selector);
        if (element != null) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                builder.append(wrapper.apply(text));
                if (!selector.contains(REGION_TEXT)) {
                    builder.append(SPACE_SEPARATOR);
                }
            }
        }
    }

    /**
     * Extracts description from generic description elements.
     *
     * <p>Looks for paragraph tags or elements with 'description' or 'excerpt'
     * in the class name.
     *
     * @param eventCard The event card container
     * @return The extracted description, or empty string if not found
     */
    private String extractDescriptionFromFallback(Element eventCard) {
        Element descElement = eventCard.selectFirst(
                PARAGRAPH + ", " + DESCRIPTION_CLASS + ", " + EXCERPT_CLASS);
        return descElement != null ? descElement.text().trim() : "";
    }
}
