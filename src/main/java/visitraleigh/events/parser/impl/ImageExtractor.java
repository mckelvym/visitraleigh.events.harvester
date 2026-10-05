package visitraleigh.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.parser.impl.CssSelectors.IMG_SRC;
import static visitraleigh.events.parser.impl.HtmlConstants.EMPTY;

import org.jsoup.nodes.Element;

/**
 * Extracts event image URLs with filtering.
 *
 * <p>This class extracts image URLs from event cards while filtering out
 * common non-event images like icons and logos.
 */
public final class ImageExtractor {

    private static final String ABS_SRC = "abs:src";
    // Image filter patterns (used for URL filtering, not CSS selectors)
    private static final String ICON_FILTER = "icon";
    private static final String LOGO_FILTER = "logo";
    private static final int MIN_IMAGE_URL_LENGTH = 20;

    /**
     * Extracts the image URL from an event card element.
     *
     * <p>This method:
     * <ul>
     *   <li>Finds the first img element with a src attribute</li>
     *   <li>Gets the absolute URL</li>
     *   <li>Filters out URLs containing "icon" or "logo"</li>
     *   <li>Filters out very short URLs (likely invalid)</li>
     * </ul>
     *
     * @param eventCard The event card container element
     * @return The extracted image URL, or empty string if not found or filtered
     * @throws NullPointerException if eventCard is null
     */
    public String extractImageUrl(Element eventCard) {
        requireNonNull(eventCard, "eventCard must not be null");
        Element imgElement = eventCard.selectFirst(IMG_SRC);

        if (imgElement != null) {
            String src = imgElement.attr(ABS_SRC);

            // Filter out icons, logos, and short URLs
            if (!src.contains(ICON_FILTER)
                && !src.contains(LOGO_FILTER)
                && src.length() > MIN_IMAGE_URL_LENGTH) {
                return src;
            }
        }

        return EMPTY;
    }
}
