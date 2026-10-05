package visitraleigh.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateExtractorTest {

    private DateExtractor extractor;

    @Test
    void extractDate_String_fromComplexHTML() {
        String html = "<div class='event-card'>"
                + "<div class='event-header'>"
                + "<h3>Event Title</h3>"
                + "</div>"
                + "<div class='event-meta'>"
                + "<time datetime='2024-06-15'>June 15, 2024</time>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("June 15, 2024");
    }

    @Test
    void extractDate_String_fromNestedTimeElement() {
        String html = "<div><div class='wrapper'><time>Nested date</time></div></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("Nested date");
    }

    @Test
    void extractDate_String_fromTimeElement() {
        String html = "<div><time>January 15, 2024</time></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("January 15, 2024");
    }

    @Test
    void extractDate_String_prefersTimeElementOverClass() {
        String html = "<div>"
                + "<time>From time element</time>"
                + "<span class='event-date'>From class</span>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("From time element");
    }

    @Test
    void extractDate_String_trimsWhitespace() {
        String html = "<div><time>  December 25, 2024  </time></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("December 25, 2024");
    }

    @Test
    void extractDate_String_withDatetimeAttribute() {
        String html = "<div><time datetime='2024-01-15T19:00'>Jan 15</time></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("Jan 15");
    }

    @Test
    void extractDate_String_withEmptyTimeElement_returnsEmpty() {
        String html = "<div><time></time></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEmpty();
    }

    @Test
    void extractDate_String_withPartialClassMatch() {
        String html = "<div><span class='meta-dateInfo-wrapper'>Partial match date</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("Partial match date");
    }

    @Test
    void extractDate_String_withWhitespaceOnlyTimeElement_returnsEmpty() {
        String html = "<div><time>   </time></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEmpty();
    }

    @Test
    void extractDate_fromCapitalizedDateStringClass() {
        String html = "<div><span class='eventDate'>March 10, 2024</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("March 10, 2024");
    }

    @Test
    void extractDate_fromDateStringClass() {
        String html = "<div><span class='event-date'>June 20, 2024</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEqualTo("June 20, 2024");
    }

    @Test
    void extractDate_withNoDateStringElement_returnsEmpty() {
        String html = "<div><p>No date here</p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String date = extractor.extractDateString(eventCard);

        assertThat(date).isEmpty();
    }

    @BeforeEach
    void setUp() {
        extractor = new DateExtractor();
    }
}
