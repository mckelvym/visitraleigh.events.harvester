package visitraleigh.events.scraper.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventLinkDiscovererTest {

    private EventLinkDiscoverer discoverer;
    private static final Pattern EVENT_URL_PATTERN =
            Pattern.compile("https://www\\.visitraleigh\\.com/event/[^/]+/\\d+/?");
    private static final String HOST_FILTER = "visitraleigh.com/event/";

    @BeforeEach
    void setUp() {
        discoverer = new EventLinkDiscoverer(EVENT_URL_PATTERN, HOST_FILTER);
    }

    @Test
    void discoverEventLinks_withValidEventLink_returnsLink() {
        String html = "<a href='https://www.visitraleigh.com/event/summer-fest/12345/'>Event</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_withMultipleLinks_returnsAll() {
        String html = "<div>"
                + "<a href='https://www.visitraleigh.com/event/event1/111/'>Event 1</a>"
                + "<a href='https://www.visitraleigh.com/event/event2/222/'>Event 2</a>"
                + "<a href='https://www.visitraleigh.com/event/event3/333/'>Event 3</a>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(3);
    }

    @Test
    void discoverEventLinks_withNoEventLinks_returnsEmpty() {
        String html = "<div><a href='https://www.example.com/page'>Non-event link</a></div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).isEmpty();
    }

    @Test
    void discoverEventLinks_withDuplicates_returnsUnique() {
        String html = "<div>"
                + "<a href='https://www.visitraleigh.com/event/concert/123/'>Concert 1</a>"
                + "<a href='https://www.visitraleigh.com/event/concert/123/'>Concert 2</a>"
                + "<a href='https://www.visitraleigh.com/event/concert/123/'>Concert 3</a>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_withWrongHost_filters() {
        String html = "<a href='https://www.wrongsite.com/event/fest/456/'>Wrong Host</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).isEmpty();
    }

    @Test
    void discoverEventLinks_withInvalidPattern_filters() {
        String html = "<a href='https://www.visitraleigh.com/event/invalid'>No ID</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).isEmpty();
    }

    @Test
    void discoverEventLinks_withTrailingSlash_accepts() {
        String html = "<a href='https://www.visitraleigh.com/event/fest/789/'>With slash</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_withoutTrailingSlash_accepts() {
        String html = "<a href='https://www.visitraleigh.com/event/fest/789'>No slash</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_withRelativeUrls_convertsToAbsolute() {
        String html = "<a href='/event/concert/456/'>Relative</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
        String href = links.get(0).attr("abs:href");
        assertThat(href).startsWith("https://www.visitraleigh.com");
    }

    @Test
    void discoverEventLinks_withMixedValidAndInvalid_filtersCorrectly() {
        String html = "<div>"
                + "<a href='https://www.visitraleigh.com/event/valid/123/'>Valid</a>"
                + "<a href='https://www.other.com/event/wrong/456/'>Wrong host</a>"
                + "<a href='https://www.visitraleigh.com/event/no-id'>No ID</a>"
                + "<a href='https://www.visitraleigh.com/event/valid2/789/'>Valid 2</a>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(2);
    }

    @Test
    void discoverEventLinks_withComplexHTML() {
        String html = "<div class='events-list'>"
                + "<div class='event-card'>"
                + "<h3><a href='https://www.visitraleigh.com/event/summer-festival/12345/'>"
                + "Summer Festival</a></h3>"
                + "</div>"
                + "<div class='event-card'>"
                + "<h3><a href='https://www.visitraleigh.com/event/art-show/67890/'>"
                + "Art Show</a></h3>"
                + "</div>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(2);
    }

    @Test
    void discoverEventLinks_withQueryParameters_accepts() {
        String html = "<a href='https://www.visitraleigh.com/event/fest/123/?ref=homepage'>Event</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_withNestedLinks_findsAll() {
        String html = "<div>"
                + "<div class='outer'>"
                + "<div class='inner'>"
                + "<a href='https://www.visitraleigh.com/event/nested/999/'>Nested</a>"
                + "</div>"
                + "</div>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }

    @Test
    void discoverEventLinks_preservesOriginalElements() {
        String html = "<a href='https://www.visitraleigh.com/event/test/111/' class='event-link' "
                + "data-id='111'>Test Event</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
        Element link = links.get(0);
        assertThat(link.className()).isEqualTo("event-link");
        assertThat(link.attr("data-id")).isEqualTo("111");
        assertThat(link.text()).isEqualTo("Test Event");
    }

    @Test
    void discoverEventLinks_withLongEventSlug_accepts() {
        String html = "<a href='https://www.visitraleigh.com/event/"
                + "very-long-event-name-with-many-words/12345/'>Long Event</a>";
        Document doc = Jsoup.parse(html, "https://www.visitraleigh.com");

        List<Element> links = discoverer.discoverEventLinks(doc);

        assertThat(links).hasSize(1);
    }
}
