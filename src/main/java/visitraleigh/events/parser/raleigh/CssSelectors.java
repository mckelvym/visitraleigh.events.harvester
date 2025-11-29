package visitraleigh.events.parser.raleigh;

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

    // String constants for description parsing
    public static final String REGION_TEXT = "region";
    public static final String SPACE_SEPARATOR = " ";
    public static final String BR_TAG = "<br/>";

    // Container class name patterns (used for string matching, not CSS selectors)
    public static final String EVENT_CLASS_PATTERN = "event";
    public static final String CARD_CLASS_PATTERN = "card";
    public static final String RESULT_CLASS_PATTERN = "result";
    public static final String LISTING_CLASS_PATTERN = "listing";
    public static final String ITEM_CLASS_PATTERN = "item";

    // Image filter patterns (used for URL filtering, not CSS selectors)
    public static final String ICON_FILTER = "icon";
    public static final String LOGO_FILTER = "logo";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
