package visitraleigh.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.CssSelectors.ARTICLE_TAG;

import org.jsoup.nodes.Element;

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
public final class EventCardFinder {

    private static final String CARD_CLASS_PATTERN = "card";
    // Container class name patterns (used for string matching, not CSS selectors)
    private static final String EVENT_CLASS_PATTERN = "event";
    private static final String ITEM_CLASS_PATTERN = "item";
    private static final String LISTING_CLASS_PATTERN = "listing";
    private static final int MAX_TRAVERSAL_DEPTH = 10;
    private static final String RESULT_CLASS_PATTERN = "result";

    /**
     * Finds the event card container for a given link element.
     *
     * <p>This method traverses up the DOM tree from the link element,
     * checking each parent to see if it's an event card container.
     *
     * @param linkElement The link element to start from
     * @return The event card container element, or the last element checked
     * if no container is found
     * @throws NullPointerException if linkElement is null
     */
    public Element findEventCardContainer(Element linkElement) {
        requireNonNull(linkElement, "linkElement must not be null");
        Element current = linkElement;

        for (int i = 0; i < MAX_TRAVERSAL_DEPTH; i++) {
            Element parent = current.parent();
            if (parent == null) {
                break;
            }

            if (isEventCardContainer(parent)) {
                return parent;
            }

            current = parent;
        }

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

        return className.contains(EVENT_CLASS_PATTERN) || className.contains(CARD_CLASS_PATTERN)
            || className.contains(RESULT_CLASS_PATTERN) || className.contains(LISTING_CLASS_PATTERN)
            || className.contains(ITEM_CLASS_PATTERN) || tagName.equals(ARTICLE_TAG);
    }
}
