package visitraleigh.events.parser.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleExtractorTest {

    private TitleExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TitleExtractor();
    }

    @Test
    void extractTitle_fromH1Heading_returnsTitle() {
        String html = "<div><h1>Summer Music Festival</h1></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Summer Music Festival");
    }

    @Test
    void extractTitle_fromH2Heading_returnsTitle() {
        String html = "<div><h2>Art Exhibition</h2></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Art Exhibition");
    }

    @Test
    void extractTitle_fromClassWithTitle_returnsTitle() {
        String html = "<div><span class='event-title'>Food Truck Rally</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Food Truck Rally");
    }

    @Test
    void extractTitle_fromClassWithName_returnsTitle() {
        String html = "<div><div class='event-name'>Farmers Market</div></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Farmers Market");
    }

    @Test
    void extractTitle_fromEventLink_returnsTitle() {
        String html = "<div><a href='/event/concert/123'>Rock Concert</a></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Rock Concert");
    }

    @Test
    void extractTitle_fromImageAlt_returnsTitle() {
        String html = "<div><img src='event.jpg' alt='Comedy Night'/></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Comedy Night");
    }

    @Test
    void extractTitle_fromAriaLabel_returnsTitle() {
        String html = "<div><a href='/event/123' aria-label='Basketball Game'>XX</a></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Basketball Game");
    }

    @Test
    void extractTitle_withTooShortTitle_returnsEmpty() {
        String html = "<div><h1>AB</h1></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isEmpty();
    }

    @Test
    void extractTitle_withExactlyMinLength_returnsEmpty() {
        String html = "<div><h1>ABC</h1></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("ABC");
    }

    @Test
    void extractTitle_withFourCharacters_returnsTitle() {
        String html = "<div><h1>ABCD</h1></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("ABCD");
    }

    @Test
    void extractTitle_withNoValidTitle_returnsEmpty() {
        String html = "<div><p>No title here</p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isEmpty();
    }

    @Test
    void extractTitle_trimsWhitespace() {
        String html = "<div><h1>  Whitespace Test  </h1></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Whitespace Test");
    }

    @Test
    void extractTitle_prefersHeadingOverOtherStrategies() {
        String html = "<div>"
                + "<h2>Heading Title</h2>"
                + "<span class='event-title'>Class Title</span>"
                + "<a href='/event/123'>Link Title</a>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Heading Title");
    }

    @Test
    void extractTitle_fallsBackToClass_whenHeadingTooShort() {
        String html = "<div>"
                + "<h2>AB</h2>"
                + "<span class='event-title'>Class Title</span>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Class Title");
    }

    @Test
    void extractTitle_fallsBackToLink_whenClassMissing() {
        String html = "<div>"
                + "<a href='/event/concert/456'>Link Text Title</a>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Link Text Title");
    }

    @Test
    void extractTitle_skipsShortLinkText_triesNextLink() {
        String html = "<div>"
                + "<a href='/event/123'>AB</a>"
                + "<a href='/event/456'>Good Link Title</a>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Good Link Title");
    }

    @Test
    void extractTitle_fromComplexRealWorldHTML() {
        String html = "<div class='event-card'>"
                + "<div class='event-image'><img src='img.jpg' alt='Event Image'/></div>"
                + "<div class='event-content'>"
                + "<h3 class='event-heading'>Raleigh Food Festival</h3>"
                + "<p class='event-date'>June 15, 2024</p>"
                + "<p class='event-description'>Join us for amazing food</p>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Raleigh Food Festival");
    }

    @Test
    void extractTitle_withCapitalizedClassNames() {
        String html = "<div><span class='EventTitle'>Capitalized Class</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Capitalized Class");
    }

    @Test
    void extractTitle_withMixedCaseClassName() {
        String html = "<div><span class='eventName'>Mixed Case Name</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Mixed Case Name");
    }

    @Test
    void extractTitle_withNestedHeadings() {
        String html = "<div>"
                + "<div class='wrapper'>"
                + "<h3>Nested Event Title</h3>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isPresent();
        assertThat(title.get()).isEqualTo("Nested Event Title");
    }

    @Test
    void extractTitle_withMultipleStrategiesFailing_returnsEmpty() {
        String html = "<div>"
                + "<h1>AB</h1>"
                + "<span class='short'>XY</span>"
                + "<a href='/event/123'>Z</a>"
                + "<img src='img.jpg' alt='NO'/>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        Optional<String> title = extractor.extractTitle(eventCard);

        assertThat(title).isEmpty();
    }
}
