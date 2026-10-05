package visitraleigh.events.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import visitraleigh.events.config.ScraperConfiguration;
import visitraleigh.events.config.impl.ScraperConfigurationImpl;
import visitraleigh.events.domain.EventItem;

/**
 * Tests for the standardized GUID and description output of RssFeedManagerImpl.
 */
class RssFeedManagerImplFormatTest {

    private static final String LINK = "https://example.com/event/1/";
    private static final String DESCRIPTION_HTML = "<p>Hello <strong>world</strong></p>";

    @TempDir
    private Path tempDir;

    private RssFeedManagerImpl feedManager;
    private Path feedFile;

    private void generate(final List<EventItem> events) throws Exception {
        feedManager.generateFeed(feedFile.toString(), events,
            tempDir.resolve("existing.xml").toString());
    }

    private Element parseSingleItem() throws Exception {
        final DocumentBuilder builder = new XmlSecurityConfigurer()
            .createSecureDocumentBuilderFactory().newDocumentBuilder();
        final Document doc = builder.parse(feedFile.toFile());
        assertThat(doc.getElementsByTagName("item").getLength()).isEqualTo(1);
        return (Element) doc.getElementsByTagName("item").item(0);
    }

    @BeforeEach
    void setUp() {
        final ScraperConfiguration config = new ScraperConfigurationImpl();
        feedManager = new RssFeedManagerImpl(config);
        feedFile = tempDir.resolve("events.xml");
    }

    @Test
    void generateFeedWritesPermalinkGuidFromEventLink() throws Exception {
        generate(List.of(new EventItem(LINK, "Title", LINK, DESCRIPTION_HTML,
            LocalDate.now().plusDays(1), null, null, null)));

        final Element guid = (Element) parseSingleItem().getElementsByTagName("guid").item(0);
        assertThat(guid.getAttribute("isPermaLink")).isEqualTo("true");
        assertThat(guid.getTextContent()).isEqualTo(LINK);
    }

    @Test
    void generateFeedWrapsDescriptionInCdata() throws Exception {
        generate(List.of(new EventItem(LINK, "Title", LINK, DESCRIPTION_HTML,
            LocalDate.now().plusDays(1), null, null, null)));

        final Element description =
            (Element) parseSingleItem().getElementsByTagName("description").item(0);
        assertThat(description.getFirstChild().getNodeType())
            .isEqualTo(Node.CDATA_SECTION_NODE);
        assertThat(description.getTextContent()).contains(DESCRIPTION_HTML);
    }

    @Test
    void generateFeedAcceptsImmutableEmptyList() throws Exception {
        generate(List.of());

        assertThat(Files.exists(feedFile)).isTrue();
    }
}
