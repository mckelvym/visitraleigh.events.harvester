package visitraleigh.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for HTML parsing logic in RaleighEventsRSSGenerator.
 *
 * <p>These tests verify the multi-fallback parsing strategies for extracting
 * event information from HTML.
 */
class HtmlParsingTest {

    @TempDir
    Path tempDir;

    private RaleighEventsRSSGenerator generator;

    @BeforeEach
    void setUp() throws IOException {
        Path testRssFile = tempDir.resolve("test-events.xml");
        generator = new RaleighEventsRSSGenerator(testRssFile.toString());
    }

    @Test
    void testGetNumPages_WithValidPagination_ShouldExtractPageNumber() throws Exception {
        // Given: HTML with pagination element
        String html = """
                <!DOCTYPE html>
                <html>
                <body>
                    <nav>
                        <li class="arrow arrow-next arrow-double">
                            <a href="?page=5">Last</a>
                        </li>
                    </nav>
                </body>
                </html>
                """;
        Document doc = Jsoup.parse(html);

        // When: Getting number of pages
        Method getNumPages = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("getNumPages", Document.class);
        getNumPages.setAccessible(true);
        int numPages = (int) getNumPages.invoke(generator, doc);

        // Then: Should extract correct page number
        assertThat(numPages).isEqualTo(5);
    }

    @Test
    void testGetNumPages_WithMissingPagination_ShouldReturnDefault() throws Exception {
        // Given: HTML without pagination element
        String html = """
                <!DOCTYPE html>
                <html>
                <body>
                    <div>No pagination here</div>
                </body>
                </html>
                """;
        Document doc = Jsoup.parse(html);

        // When: Getting number of pages
        Method getNumPages = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("getNumPages", Document.class);
        getNumPages.setAccessible(true);
        int numPages = (int) getNumPages.invoke(generator, doc);

        // Then: Should return default value of 10
        assertThat(numPages).isEqualTo(10);
    }

    @Test
    void testParsePageNumberFromHref_WithValidHref_ShouldExtractNumber() throws Exception {
        // Given: Valid href with page parameter
        String href = "?page=15";

        // When: Parsing page number
        Method parsePageNumber = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("parsePageNumberFromHref", String.class);
        parsePageNumber.setAccessible(true);
        int pageNum = (int) parsePageNumber.invoke(generator, href);

        // Then: Should extract correct number
        assertThat(pageNum).isEqualTo(15);
    }

    @Test
    void testParsePageNumberFromHref_WithMultipleParams_ShouldExtractNumber()
            throws Exception {
        // Given: Href with multiple parameters
        String href = "/events?category=music&page=7&sort=date";

        // When: Parsing page number
        Method parsePageNumber = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("parsePageNumberFromHref", String.class);
        parsePageNumber.setAccessible(true);
        int pageNum = (int) parsePageNumber.invoke(generator, href);

        // Then: Should extract correct number
        assertThat(pageNum).isEqualTo(7);
    }

    @Test
    void testParsePageNumberFromHref_WithInvalidHref_ShouldReturnDefault()
            throws Exception {
        // Given: Href without page parameter
        String href = "/events?category=music";

        // When: Parsing page number
        Method parsePageNumber = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("parsePageNumberFromHref", String.class);
        parsePageNumber.setAccessible(true);
        int pageNum = (int) parsePageNumber.invoke(generator, href);

        // Then: Should return default value of 10
        assertThat(pageNum).isEqualTo(10);
    }

    @Test
    void testExtractEventId_WithValidUrl_ShouldExtractId() throws Exception {
        // Given: Valid event URL
        String eventUri = "https://www.visitraleigh.com/event/sample-event/12345/";

        // When: Extracting event ID
        Method extractEventId = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("extractEventId", String.class);
        extractEventId.setAccessible(true);
        int eventId = (int) extractEventId.invoke(generator, eventUri);

        // Then: Should extract correct ID
        assertThat(eventId).isEqualTo(12345);
    }

    @Test
    void testExtractEventId_WithoutTrailingSlash_ShouldExtractId() throws Exception {
        // Given: Event URL without trailing slash
        String eventUri = "https://www.visitraleigh.com/event/sample-event/99999";

        // When: Extracting event ID
        Method extractEventId = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("extractEventId", String.class);
        extractEventId.setAccessible(true);
        int eventId = (int) extractEventId.invoke(generator, eventUri);

        // Then: Should extract correct ID
        assertThat(eventId).isEqualTo(99999);
    }

    @Test
    void testIsEventCardContainer_WithEventClass_ShouldReturnTrue() throws Exception {
        // Given: Element with "event" in class name
        String html = "<article class=\"event-card\"><h2>Test</h2></article>";
        Document doc = Jsoup.parse(html);
        Element element = doc.selectFirst("article");

        // When: Checking if it's an event card container
        Method isEventCardContainer = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("isEventCardContainer", Element.class);
        isEventCardContainer.setAccessible(true);
        boolean result = (boolean) isEventCardContainer.invoke(generator, element);

        // Then: Should return true
        assertThat(result).isTrue();
    }

    @Test
    void testIsEventCardContainer_WithCardClass_ShouldReturnTrue() throws Exception {
        // Given: Element with "card" in class name
        String html = "<div class=\"card item-card\"><h2>Test</h2></div>";
        Document doc = Jsoup.parse(html);
        Element element = doc.selectFirst("div");

        // When: Checking if it's an event card container
        Method isEventCardContainer = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("isEventCardContainer", Element.class);
        isEventCardContainer.setAccessible(true);
        boolean result = (boolean) isEventCardContainer.invoke(generator, element);

        // Then: Should return true
        assertThat(result).isTrue();
    }

    @Test
    void testIsEventCardContainer_WithArticleTag_ShouldReturnTrue() throws Exception {
        // Given: article tag element
        String html = "<article><h2>Test</h2></article>";
        Document doc = Jsoup.parse(html);
        Element element = doc.selectFirst("article");

        // When: Checking if it's an event card container
        Method isEventCardContainer = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("isEventCardContainer", Element.class);
        isEventCardContainer.setAccessible(true);
        boolean result = (boolean) isEventCardContainer.invoke(generator, element);

        // Then: Should return true
        assertThat(result).isTrue();
    }

    @Test
    void testIsEventCardContainer_WithIrrelevantElement_ShouldReturnFalse()
            throws Exception {
        // Given: Element without event-related classes
        String html = "<div class=\"navigation-bar\"><h2>Test</h2></div>";
        Document doc = Jsoup.parse(html);
        Element element = doc.selectFirst("div");

        // When: Checking if it's an event card container
        Method isEventCardContainer = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("isEventCardContainer", Element.class);
        isEventCardContainer.setAccessible(true);
        boolean result = (boolean) isEventCardContainer.invoke(generator, element);

        // Then: Should return false
        assertThat(result).isFalse();
    }

    @Test
    void testFindEventCardContainer_ShouldTraverseUpToParent() throws Exception {
        // Given: Nested HTML structure
        String html = """
                <article class="event-item">
                    <div class="content">
                        <div class="inner">
                            <a href="/event/test/123">Test Event</a>
                        </div>
                    </div>
                </article>
                """;
        Document doc = Jsoup.parse(html);
        Element linkElement = doc.selectFirst("a");

        // When: Finding event card container
        Method findEventCardContainer = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("findEventCardContainer", Element.class);
        findEventCardContainer.setAccessible(true);
        Element container = (Element) findEventCardContainer.invoke(generator, linkElement);

        // Then: Should find the article parent
        assertThat(container.tagName()).isEqualTo("article");
        assertThat(container.className()).contains("event-item");
    }
}
