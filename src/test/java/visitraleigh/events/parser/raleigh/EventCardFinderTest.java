package visitraleigh.events.parser.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventCardFinderTest {

    private EventCardFinder finder;

    @BeforeEach
    void setUp() {
        finder = new EventCardFinder();
    }

    @Test
    void findEventCardContainer_withEventClass_findsContainer() {
        String html = "<div class='event-card'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("event-card");
    }

    @Test
    void findEventCardContainer_withCardClass_findsContainer() {
        String html = "<div class='card'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("card");
    }

    @Test
    void findEventCardContainer_withResultClass_findsContainer() {
        String html = "<div class='search-result'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("result");
    }

    @Test
    void findEventCardContainer_withListingClass_findsContainer() {
        String html = "<div class='event-listing'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("listing");
    }

    @Test
    void findEventCardContainer_withItemClass_findsContainer() {
        String html = "<div class='list-item'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("item");
    }

    @Test
    void findEventCardContainer_withArticleTag_findsContainer() {
        String html = "<article><a href='/event/123'>Event Link</a></article>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.tagName()).isEqualTo("article");
    }

    @Test
    void findEventCardContainer_traversesMultipleLevels() {
        String html = "<div class='event-card'>"
                + "<div class='inner'>"
                + "<div class='content'>"
                + "<a href='/event/123'>Event Link</a>"
                + "</div></div></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("event-card");
    }

    @Test
    void findEventCardContainer_stopsAtFirstMatch() {
        String html = "<div class='outer-event'>"
                + "<div class='inner-card'>"
                + "<a href='/event/123'>Event Link</a>"
                + "</div></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("inner-card");
    }

    @Test
    void findEventCardContainer_withNoMatchingParent_returnsLast() {
        String html = "<div class='wrapper'>"
                + "<div class='container'>"
                + "<a href='/event/123'>Event Link</a>"
                + "</div></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container).isNotNull();
    }

    @Test
    void findEventCardContainer_withDirectParent_findsImmediately() {
        String html = "<article><a href='/event/123'>Event Link</a></article>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.tagName()).isEqualTo("article");
    }

    @Test
    void findEventCardContainer_caseInsensitiveMatch() {
        String html = "<div class='EVENT-Card'><a href='/event/123'>Event Link</a></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).containsIgnoringCase("event");
    }

    @Test
    void findEventCardContainer_withComplexNesting() {
        String html = "<div class='page'>"
                + "<div class='content'>"
                + "<div class='event-list'>"
                + "<div class='wrapper'>"
                + "<div class='inner'>"
                + "<a href='/event/123'>Event Link</a>"
                + "</div></div></div></div></div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("event-list");
    }

    @Test
    void findEventCardContainer_respectsMaxDepth() {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            html.append("<div class='level").append(i).append("'>");
        }
        html.append("<a href='/event/123'>Event Link</a>");
        for (int i = 0; i < 15; i++) {
            html.append("</div>");
        }

        Element linkElement = Jsoup.parse(html.toString()).select("a").first();
        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container).isNotNull();
    }

    @Test
    void findEventCardContainer_withRealWorldHTML() {
        String html = "<div class='search-results'>"
                + "<div class='result-item event-card'>"
                + "<div class='image-wrapper'>"
                + "<img src='event.jpg'/>"
                + "</div>"
                + "<div class='event-details'>"
                + "<h3><a href='/event/summer-fest/12345'>Summer Festival</a></h3>"
                + "<p class='date'>June 15</p>"
                + "</div>"
                + "</div>"
                + "</div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).containsAnyOf("event-card", "event-details");
    }

    @Test
    void findEventCardContainer_withMultipleClassNames() {
        String html = "<div class='col-md-4 event-listing featured'>"
                + "<a href='/event/123'>Event Link</a>"
                + "</div>";
        Element linkElement = Jsoup.parse(html).select("a").first();

        Element container = finder.findEventCardContainer(linkElement);

        assertThat(container.className()).contains("event-listing");
    }
}
