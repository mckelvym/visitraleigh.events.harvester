package visitraleigh.events.parser.raleigh;

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
public class TitleExtractor {

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
     */
    public Optional<String> extractTitle(Element eventCard) {
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
            logEventCardDebugInfo(eventCard);
            return Optional.empty();
        }

        LOG.debug("Extracted title: {}", title);
        return Optional.of(title);
    }

    /**
     * Strategy 1: Extract title from heading tags (h1-h6).
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromHeadings(Element eventCard) {
        Element heading = eventCard.selectFirst("h1, h2, h3, h4, h5, h6");
        if (heading != null) {
            String title = heading.text().trim();
            LOG.trace("Found title from heading <{}>: {}", heading.tagName(), title);
            return title;
        }
        return "";
    }

    /**
     * Strategy 2: Extract title from elements with 'title' or 'name' in class.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromClass(Element eventCard) {
        Element titleElem = eventCard.selectFirst(
                "[class*='title'], [class*='Title'], [class*='name'], [class*='Name']");
        if (titleElem != null) {
            String title = titleElem.text().trim();
            LOG.trace("Found title from class: {}", title);
            return title;
        }
        return "";
    }

    /**
     * Strategy 3: Extract title from event link text.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromLinks(Element eventCard) {
        Elements links = eventCard.select("a[href*='/event/']");
        for (Element link : links) {
            String linkText = link.text().trim();
            if (linkText.length() > MIN_TITLE_LENGTH) {
                LOG.trace("Found title from link text: {}", linkText);
                return linkText;
            }
        }
        return "";
    }

    /**
     * Strategy 4: Extract title from image alt attribute.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromImage(Element eventCard) {
        Element img = eventCard.selectFirst("img[alt]");
        if (img != null) {
            String alt = img.attr("alt").trim();
            if (alt.length() > MIN_TITLE_LENGTH) {
                LOG.trace("Found title from image alt: {}", alt);
                return alt;
            }
        }
        return "";
    }

    /**
     * Strategy 5: Extract title from aria-label attribute.
     *
     * @param eventCard The event card container
     * @return The extracted title, or empty string if not found
     */
    private String extractTitleFromAriaLabel(Element eventCard) {
        Elements linksWithAria = eventCard.select("a[aria-label]");
        for (Element link : linksWithAria) {
            String ariaLabel = link.attr("aria-label").trim();
            if (ariaLabel.length() > MIN_TITLE_LENGTH) {
                LOG.trace("Found title from aria-label: {}", ariaLabel);
                return ariaLabel;
            }
        }
        return "";
    }

    /**
     * Logs debug information when title extraction fails.
     *
     * @param eventCard The event card that failed title extraction
     */
    private void logEventCardDebugInfo(Element eventCard) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Could not extract title from event card");
            String eventCardHtml = eventCard.html();
            LOG.trace("Event card HTML (first 300 chars): {}",
                    eventCardHtml.substring(0, Math.min(300, eventCardHtml.length())));
        }
    }
}
