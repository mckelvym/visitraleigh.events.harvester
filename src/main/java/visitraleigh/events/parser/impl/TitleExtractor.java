package visitraleigh.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.CssSelectors.ARIA_LABEL;
import static visitraleigh.events.parser.impl.CssSelectors.EVENT_LINK;
import static visitraleigh.events.parser.impl.CssSelectors.HEADINGS;
import static visitraleigh.events.parser.impl.CssSelectors.IMAGE_ALT;
import static visitraleigh.events.parser.impl.CssSelectors.NAME_CLASS;
import static visitraleigh.events.parser.impl.CssSelectors.TITLE_CLASS;
import static visitraleigh.events.parser.impl.HtmlConstants.ALT_ATTR;
import static visitraleigh.events.parser.impl.HtmlConstants.ARIA_LABEL_ATTR;
import static visitraleigh.events.parser.impl.HtmlConstants.EMPTY;

import java.util.Optional;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event titles using multiple fallback strategies.
 *
 * <p>This class implements a robust multi-strategy approach to extracting
 * event titles from HTML. It tries 5 different extraction methods in order:
 * <ol>
 *   <li>From heading tags (h1-h6)</li>
 *   <li>From elements with 'title' or 'name' in class name</li>
 *   <li>From event link text</li>
 *   <li>From image alt attributes</li>
 *   <li>From aria-label attributes</li>
 * </ol>
 *
 * <p>Each strategy is tried in sequence until a valid title (length > 3) is found.
 * If all strategies fail, an empty Optional is returned.
 */
public final class TitleExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TitleExtractor.class);
    private static final int MIN_TITLE_LENGTH = 3;

    /**
     * Extracts the title from an event card element.
     *
     * <p>This method tries multiple extraction strategies in order until
     * a valid title is found.
     *
     * @param eventCard The event card container element
     * @return Optional containing the extracted title, or empty if extraction fails
     * @throws NullPointerException if eventCard is null
     */
    public Optional<String> extractTitle(Element eventCard) {
        requireNonNull(eventCard, "eventCard must not be null");
        String title = extractTitleFromHeadings(eventCard);

        if (title.length() < MIN_TITLE_LENGTH) {
            title = extractTitleFromClass(eventCard);
        }

        if (title.length() < MIN_TITLE_LENGTH) {
            title = extractTitleFromLinks(eventCard);
        }

        if (title.length() < MIN_TITLE_LENGTH) {
            title = extractTitleFromImage(eventCard);
        }

        if (title.length() < MIN_TITLE_LENGTH) {
            title = extractTitleFromAriaLabel(eventCard);
        }

        if (title.length() < MIN_TITLE_LENGTH) {
            return Optional.empty();
        }

        return Optional.of(title);
    }

    /**
     * Strategy 5: Extract title from aria-label attribute.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromAriaLabel(Element eventCard) {
        Elements linksWithAria = eventCard.select(ARIA_LABEL);
        for (Element link : linksWithAria) {
            String ariaLabel = link.attr(ARIA_LABEL_ATTR).trim();
            if (ariaLabel.length() > MIN_TITLE_LENGTH) {
                return ariaLabel;
            }
        }
        return EMPTY;
    }

    /**
     * Strategy 2: Extract title from elements with 'title' or 'name' in class.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromClass(Element eventCard) {
        Element titleElem = eventCard.selectFirst(TITLE_CLASS + ", " + NAME_CLASS);
        if (titleElem != null) {
            return titleElem.text().trim();
        }
        return EMPTY;
    }

    /**
     * Strategy 1: Extract title from heading tags (h1-h6).
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromHeadings(Element eventCard) {
        Element heading = eventCard.selectFirst(HEADINGS);
        if (heading != null) {
            return heading.text().trim();
        }
        return EMPTY;
    }

    /**
     * Strategy 4: Extract title from image alt attribute.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromImage(Element eventCard) {
        Element img = eventCard.selectFirst(IMAGE_ALT);
        if (img != null) {
            String alt = img.attr(ALT_ATTR).trim();
            if (alt.length() > MIN_TITLE_LENGTH) {
                return alt;
            }
        }
        return EMPTY;
    }

    /**
     * Strategy 3: Extract title from event link text.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromLinks(Element eventCard) {
        Elements links = eventCard.select(EVENT_LINK);
        for (Element link : links) {
            String linkText = link.text().trim();
            if (linkText.length() > MIN_TITLE_LENGTH) {
                return linkText;
            }
        }
        return EMPTY;
    }
}
