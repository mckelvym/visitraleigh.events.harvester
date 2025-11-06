package visitraleigh.events.scraper.raleigh;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses pagination information from event listing pages.
 *
 * <p>This class extracts the total number of pages from pagination controls
 * on event listing pages. It looks for the "last page" link and extracts
 * the page number from its URL.
 *
 * <p>If pagination cannot be determined, it falls back to a default value.
 */
public class PaginationParser {

    private static final Logger LOG = LoggerFactory.getLogger(PaginationParser.class);

    private final String lastPageLinkSelector;
    private final Pattern numPagesPattern;
    private final int defaultNumPages;

    /**
     * Creates a new pagination parser with the specified configuration.
     *
     * @param lastPageLinkSelector CSS selector for the last page link element
     * @param numPagesPattern Regex pattern for extracting page numbers from URLs
     * @param defaultNumPages Default number of pages if extraction fails
     */
    public PaginationParser(
            String lastPageLinkSelector,
            Pattern numPagesPattern,
            int defaultNumPages) {
        this.lastPageLinkSelector = lastPageLinkSelector;
        this.numPagesPattern = numPagesPattern;
        this.defaultNumPages = defaultNumPages;
    }

    /**
     * Extracts the number of pages from a document.
     *
     * <p>This method:
     * <ol>
     *   <li>Selects the last page navigation element using CSS selector</li>
     *   <li>Extracts the href from the first child element</li>
     *   <li>Parses the page number from the href using regex</li>
     *   <li>Returns default value if any step fails</li>
     * </ol>
     *
     * @param doc The JSoup document containing pagination controls
     * @return The number of pages, or default value if extraction fails
     */
    public int getNumPages(Document doc) {
        Elements doubleArrow = doc.select(lastPageLinkSelector);
        if (doubleArrow.isEmpty()) {
            LOG.debug("No pagination element found, using default: {}", defaultNumPages);
            return defaultNumPages;
        }

        String href = extractHrefFromPaginationElement(doubleArrow);
        if (href == null) {
            LOG.debug("No href found in pagination element, using default: {}",
                    defaultNumPages);
            return defaultNumPages;
        }

        return parsePageNumberFromHref(href);
    }

    /**
     * Extracts the href attribute from the pagination element.
     *
     * @param doubleArrow The pagination elements
     * @return The href value, or null if not found
     */
    private String extractHrefFromPaginationElement(Elements doubleArrow) {
        Element first = doubleArrow.first();
        if (first == null) {
            return null;
        }

        Elements children = first.children();
        if (children.isEmpty()) {
            return null;
        }

        Element firstChild = children.first();
        if (firstChild == null) {
            return null;
        }

        String href = firstChild.attr("href");
        return href.isEmpty() ? null : href;
    }

    /**
     * Parses the page number from an href string using regex.
     *
     * @param href The href string containing the page parameter
     * @return The extracted page number, or default value if parsing fails
     */
    private int parsePageNumberFromHref(String href) {
        Matcher matcher = numPagesPattern.matcher(href);

        if (!matcher.find()) {
            LOG.debug("No page parameter found in href: {}", href);
            return defaultNumPages;
        }

        String pageNumber = matcher.group(1);
        try {
            int num = Integer.parseInt(pageNumber);
            LOG.debug("Extracted {} pages from pagination", num);
            return num;
        } catch (NumberFormatException e) {
            LOG.error("Failed to parse page number from: {}", pageNumber, e);
            return defaultNumPages;
        }
    }
}
