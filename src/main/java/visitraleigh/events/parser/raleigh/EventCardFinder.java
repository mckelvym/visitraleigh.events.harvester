package visitraleigh.events.parser.raleigh;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.raleigh.CssSelectors.ARTICLE_TAG;
import static visitraleigh.events.parser.raleigh.CssSelectors.CARD_CLASS_PATTERN;
import static visitraleigh.events.parser.raleigh.CssSelectors.EVENT_CLASS_PATTERN;
import static visitraleigh.events.parser.raleigh.CssSelectors.ITEM_CLASS_PATTERN;
import static visitraleigh.events.parser.raleigh.CssSelectors.LISTING_CLASS_PATTERN;
import static visitraleigh.events.parser.raleigh.CssSelectors.RESULT_CLASS_PATTERN;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds event card containers by traversing the DOM upward.
 *
 * <p>This class locates the main container element for an event by starting
 * from a link element and traversing up through parent elements. It identifies
 * containers using heuristics based on class names and tag names.
 *
 * <p>The traversal stops when:
 * <ul>
 *   <li>An event card container is found (matching heuristics)</li>
 *   <li>The maximum traversal depth is reached (10 levels)</li>
 *   <li>There are no more parent elements</li>
 * </ul>
 */
public class EventCardFinder {

    private static final Logger LOG = LoggerFactory.getLogger(EventCardFinder.class);
    private static final int MAX_TRAVERSAL_DEPTH = 10;

    /**
     * Finds the event card container for a given link element.
     *
     * <p>This method traverses up the DOM tree from the link element,
     * checking each parent to see if it's an event card container.
     *
     * @param linkElement The link element to start from
     * @return The event card container element, or the last element checked
     *         if no container is found
     * @throws NullPointerException if linkElement is null
     */
    public Element findEventCardContainer(Element linkElement) {
        requireNonNull(linkElement, "linkElement must not be null");
        Element current = linkElement;

        for (int i = 0; i < MAX_TRAVERSAL_DEPTH; i++) {
            Element parent = current.parent();
            if (parent == null) {
                LOG.debug("Reached root element after {} levels", i);
                break;
            }

            if (isEventCardContainer(parent)) {
                LOG.debug("Found event card container: <{}> with class '{}'",
                        parent.tagName(), parent.className());
                return parent;
            }

            current = parent;
        }

        LOG.debug("No event card container found, using current element: <{}>",
                current.tagName());
        return current;
    }

    /**
     * Determines whether an element is an event card container.
     *
     * <p>An element is considered an event card container if its class name
     * or tag name matches common event card patterns:
     * <ul>
     *   <li>Class contains: "event", "card", "result", "listing", or "item"</li>
     *   <li>Tag name is: "article"</li>
     * </ul>
     *
     * @param element The element to check
     * @return true if the element is an event card container, false otherwise
     */
    private boolean isEventCardContainer(Element element) {
        String className = element.className().toLowerCase();
        String tagName = element.tagName().toLowerCase();

        boolean isContainer = className.contains(EVENT_CLASS_PATTERN)
                || className.contains(CARD_CLASS_PATTERN)
                || className.contains(RESULT_CLASS_PATTERN)
                || className.contains(LISTING_CLASS_PATTERN)
                || className.contains(ITEM_CLASS_PATTERN)
                || tagName.equals(ARTICLE_TAG);

        if (isContainer) {
            LOG.trace("Element matches container pattern: <{}> class='{}'",
                    tagName, className);
        }

        return isContainer;
    }
}
