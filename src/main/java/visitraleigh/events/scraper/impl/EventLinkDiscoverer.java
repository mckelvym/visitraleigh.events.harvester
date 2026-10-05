package visitraleigh.events.scraper.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.CssSelectors.EVENT_LINK;
import static visitraleigh.events.parser.impl.HtmlConstants.ABS_HREF_ATTR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers event links on listing pages.
 *
 * <p>This class is responsible for finding and filtering event URLs from
 * HTML documents. It:
 * <ul>
 *   <li>Selects all links containing '/event/' in the href</li>
 *   <li>Validates URLs against the event URL pattern</li>
 *   <li>Deduplicates URLs within a single page</li>
 *   <li>Returns unique, valid event link elements</li>
 * </ul>
 */
public record EventLinkDiscoverer(Pattern eventUrlPattern, String hostFilter) {

    private static final Logger LOG = LoggerFactory.getLogger(EventLinkDiscoverer.class);

    public EventLinkDiscoverer {
        requireNonNull(eventUrlPattern, "eventUrlPattern must not be null");
        requireNonNull(hostFilter, "hostFilter must not be null");
    }

    /**
     * Discovers event links on a page.
     *
     * <p>This method selects all links containing '/event/' and filters
     * them to find valid, unique event URLs.
     *
     * @param doc The JSoup document to search
     * @return List of Element objects representing valid event links
     * @throws NullPointerException if doc is null
     */
    public List<Element> discoverEventLinks(Document doc) {
        requireNonNull(doc, "doc must not be null");
        Elements allLinks = doc.select(EVENT_LINK);

        List<Element> eventLinks = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>();

        for (Element link : allLinks) {
            String href = link.attr(ABS_HREF_ATTR);

            if (shouldProcessEventLink(href, processedUrls)) {
                processedUrls.add(href);
                eventLinks.add(link);
            }
        }

        LOG.info("Discovered {} event links on page", eventLinks.size());
        return eventLinks;
    }

    /**
     * Determines whether an event link should be processed.
     *
     * <p>A link is processed if:
     * <ul>
     *   <li>It hasn't been processed yet (not in processedUrls)</li>
     *   <li>It contains the host filter string</li>
     *   <li>It matches the event URL pattern</li>
     * </ul>
     *
     * @param href          The absolute href to check
     * @param processedUrls Set of already processed URLs
     * @return true if the link should be processed, false otherwise
     */
    private boolean shouldProcessEventLink(String href, Set<String> processedUrls) {
        // Check if already processed
        if (processedUrls.contains(href)) {
            return false;
        }

        // Check if contains host filter
        if (!href.contains(hostFilter)) {
            return false;
        }

        // Check if matches event URL pattern
        return eventUrlPattern.matcher(href).find();
    }
}
