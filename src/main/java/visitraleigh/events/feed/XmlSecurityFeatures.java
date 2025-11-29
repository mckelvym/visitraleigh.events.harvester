package visitraleigh.events.feed;

/**
 * Constants for XML security feature URIs.
 *
 * <p>These feature strings are used to configure XML parsers and transformers
 * to prevent XXE (XML External Entity) attacks and other security vulnerabilities.
 *
 * <p>All features should be disabled (set to false) to ensure secure XML processing.
 *
 * @see XmlSecurityConfigurer
 */
public final class XmlSecurityFeatures {

    /**
     * Feature to disallow DOCTYPE declarations entirely.
     * This is the most secure approach as it prevents all DOCTYPE-based attacks.
     */
    public static final String DISALLOW_DOCTYPE_DECL =
            "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * Feature to disable external general entities.
     * Prevents XXE attacks via general entity expansion.
     */
    public static final String EXTERNAL_GENERAL_ENTITIES =
            "http://xml.org/sax/features/external-general-entities";

    /**
     * Feature to disable external parameter entities.
     * Prevents XXE attacks via parameter entity expansion.
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES =
            "http://xml.org/sax/features/external-parameter-entities";

    /**
     * Feature to disable loading external DTDs.
     * Prevents fetching external DTD files during parsing.
     */
    public static final String LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Attribute name for accessing external DTDs in transformers.
     */
    public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property"
            + "/accessExternalDTD";

    /**
     * Attribute name for accessing external stylesheets in transformers.
     */
    public static final String ACCESS_EXTERNAL_STYLESHEET = "http://javax.xml.XMLConstants"
            + "/property/accessExternalStylesheet";

    /**
     * Empty string value used to restrict external access.
     */
    public static final String EMPTY_VALUE = "";

    private XmlSecurityFeatures() {
        // Utility class - prevent instantiation
    }
}
