package visitraleigh.events.parser.impl;

/**
 * Constants for CSS selectors used in HTML parsing.
 *
 * <p>This class centralizes all CSS selector strings used throughout the
 * Raleigh event parser implementation to avoid magic strings and improve
 * maintainability.
 *
 * <p>Selectors are organized by their domain: container detection, title
 * extraction, date extraction, description extraction, image extraction,
 * and link discovery.
 */
public final class CssSelectors {

    // Event card container detection
    public static final String ARTICLE_TAG = "article";

    // Title extraction selectors
    public static final String HEADINGS = "h1, h2, h3, h4, h5, h6";
    public static final String TITLE_CLASS = "[class*='title'], [class*='Title']";
    public static final String NAME_CLASS = "[class*='name'], [class*='Name']";
    public static final String EVENT_LINK = "a[href*='/event/']";
    public static final String IMAGE_ALT = "img[alt]";
    public static final String ARIA_LABEL = "a[aria-label]";

    // Date extraction selectors
    public static final String TIME_ELEMENT = "time";
    public static final String DATE_CLASS = "[class*='date']";
    public static final String DATE_CLASS_CAPITALIZED = "[class*='Date']";

    // Description extraction selectors
    public static final String BLOCK_META_DIV = "div.block-meta";
    public static final String BLOCK_META_CLASS = "[class*='block-meta']";
    public static final String DATE_INFO_CLASS = "[class*='dateInfo'], [class*='date-info']";
    public static final String TIMES_CLASS = "[class*='times'], time";
    public static final String LOCATION_CLASS = "[class*='location']";
    public static final String REGION_CLASS = "[class*='region']";
    public static final String PARAGRAPH = "p";
    public static final String DESCRIPTION_CLASS = "[class*='description']";
    public static final String EXCERPT_CLASS = "[class*='excerpt']";

    // Pagination selectors
    public static final String LAST_PAGE_LINK_ELEMENT = "li.arrow.arrow-next.arrow-double";

    // Image selectors
    public static final String IMG_SRC = "img[src]";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
