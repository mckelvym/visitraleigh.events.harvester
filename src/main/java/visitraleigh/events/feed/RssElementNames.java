package visitraleigh.events.feed;

/**
 * Constants for RSS 2.0 XML element and attribute names.
 *
 * <p>This class centralizes all XML element and attribute name strings
 * used in RSS feed generation and parsing to avoid magic strings and
 * improve maintainability.
 */
public final class RssElementNames {

    // RSS root element and attributes
    public static final String RSS = "rss";
    public static final String VERSION_ATTR = "version";
    public static final String RSS_VERSION = "2.0";

    // Channel-level elements
    public static final String CHANNEL = "channel";
    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String DESCRIPTION = "description";
    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_VALUE = "en-us";
    public static final String LAST_BUILD_DATE = "lastBuildDate";

    // Item-level elements
    public static final String ITEM = "item";
    public static final String GUID = "guid";
    public static final String PUB_DATE = "pubDate";

    // GUID attributes
    public static final String IS_PERMALINK_ATTR = "isPermaLink";
    public static final String TRUE_VALUE = "true";

    // RSS Event module (http://purl.org/rss/1.0/modules/event/), used for retention
    public static final String XMLNS_EV_ATTR = "xmlns:ev";
    public static final String EVENT_NAMESPACE_URI = "http://purl.org/rss/1.0/modules/event/";
    public static final String EV_STARTDATE = "ev:startdate";
    public static final String EV_ENDDATE = "ev:enddate";

    // Enclosure (image) elements and attributes
    public static final String ENCLOSURE = "enclosure";
    public static final String URL_ATTR = "url";
    public static final String TYPE_ATTR = "type";
    public static final String IMAGE_JPEG_TYPE = "image/jpeg";

    // XML transformation properties
    public static final String XSLT_INDENT_PROPERTY =
            "{http://xml.apache.org/xslt}indent-amount";
    public static final String INDENT_AMOUNT = "2";
    public static final String ENCODING_UTF8 = "UTF-8";

    private RssElementNames() {
        // Utility class - prevent instantiation
    }
}
