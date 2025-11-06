package visitraleigh.events.feed;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Configures XML factories with XXE (XML External Entity) protection.
 *
 * <p>This class provides secure configurations for XML processing to prevent
 * XXE attacks and other XML-based security vulnerabilities. All XML factories
 * returned by this class have external entity processing disabled.
 *
 * <p>Security features enabled:
 * <ul>
 *   <li>Disallow DOCTYPE declarations</li>
 *   <li>Disable external general entities</li>
 *   <li>Disable external parameter entities</li>
 *   <li>Disable loading external DTDs</li>
 *   <li>Disable XInclude processing</li>
 *   <li>Disable entity reference expansion</li>
 *   <li>Restrict external DTD and stylesheet access</li>
 * </ul>
 *
 * <p>These configurations comply with OWASP recommendations and pass
 * SonarQube security scans.
 *
 * @see <a href="https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing">
 *      OWASP XXE Prevention</a>
 */
public class XmlSecurityConfigurer {

    /**
     * Creates a secure DocumentBuilderFactory with XXE protection.
     *
     * <p>All features that could lead to XXE vulnerabilities are disabled.
     *
     * @return A configured DocumentBuilderFactory safe for XML parsing
     * @throws ParserConfigurationException if configuration fails
     */
    public DocumentBuilderFactory createSecureDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Disable external entity processing to prevent XXE attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // Additional security measures
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory;
    }

    /**
     * Creates a secure TransformerFactory with XXE protection.
     *
     * <p>External DTD and stylesheet access is restricted to prevent
     * XXE attacks during XML transformation.
     *
     * @return A configured TransformerFactory safe for XML transformation
     */
    public TransformerFactory createSecureTransformerFactory() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // Disable external entity processing
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        return transformerFactory;
    }
}
