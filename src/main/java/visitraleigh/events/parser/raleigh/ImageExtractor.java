package visitraleigh.events.parser.raleigh;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event image URLs with filtering.
 *
 * <p>This class extracts image URLs from event cards while filtering out
 * common non-event images like icons and logos.
 */
public class ImageExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ImageExtractor.class);
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
     */
    public String extractImageUrl(Element eventCard) {
        Element imgElement = eventCard.selectFirst("img[src]");

        if (imgElement != null) {
            String src = imgElement.attr("abs:src");

            // Filter out icons, logos, and short URLs
            if (!src.contains("icon")
                    && !src.contains("logo")
                    && src.length() > MIN_IMAGE_URL_LENGTH) {
                LOG.debug("Extracted image URL: {}", src);
                return src;
            } else {
                LOG.trace("Filtered out image URL: {} (icon/logo or too short)", src);
            }
        }

        LOG.debug("No suitable image found in event card");
        return "";
    }
}
