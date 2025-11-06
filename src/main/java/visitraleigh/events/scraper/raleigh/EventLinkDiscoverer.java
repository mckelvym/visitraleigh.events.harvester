package visitraleigh.events.scraper.raleigh;

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
public class EventLinkDiscoverer {

    private static final Logger LOG = LoggerFactory.getLogger(EventLinkDiscoverer.class);

    private final Pattern eventUrlPattern;
    private final String hostFilter;

    /**
     * Creates a new event link discoverer.
     *
     * @param eventUrlPattern The regex pattern for validating event URLs
     * @param hostFilter The host string that must appear in valid URLs
     *                   (e.g., "visitraleigh.com/event/")
     */
    public EventLinkDiscoverer(Pattern eventUrlPattern, String hostFilter) {
        this.eventUrlPattern = eventUrlPattern;
        this.hostFilter = hostFilter;
    }

    /**
     * Discovers event links on a page.
     *
     * <p>This method selects all links containing '/event/' and filters
     * them to find valid, unique event URLs.
     *
     * @param doc The JSoup document to search
     * @return List of Element objects representing valid event links
     */
    public List<Element> discoverEventLinks(Document doc) {
        Elements allLinks = doc.select("a[href*='/event/']");
        LOG.debug("Found {} links containing '/event/'", allLinks.size());

        List<Element> eventLinks = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>();

        for (Element link : allLinks) {
            String href = link.attr("abs:href");

            if (shouldProcessEventLink(href, processedUrls)) {
                processedUrls.add(href);
                eventLinks.add(link);
                LOG.debug("Discovered event link: {}", href);
            }
        }

        LOG.info("Discovered {} unique event links on page", eventLinks.size());
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
     * @param href The absolute href to check
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
            LOG.trace("Skipping link (wrong host): {}", href);
            return false;
        }

        // Check if matches event URL pattern
        if (!eventUrlPattern.matcher(href).find()) {
            LOG.trace("Skipping link (pattern mismatch): {}", href);
            return false;
        }

        return true;
    }
}
