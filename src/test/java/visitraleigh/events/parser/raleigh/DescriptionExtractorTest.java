package visitraleigh.events.parser.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptionExtractorTest {

    private DescriptionExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new DescriptionExtractor();
    }

    @Test
    void extractDescription_fromBlockMetaWithDate() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'>January 15, 2024</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("January 15, 2024");
        assertThat(description).startsWith("<br/>");
    }

    @Test
    void extractDescription_fromBlockMetaWithMultipleFields() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'>January 15, 2024</div>"
                + "<time>7:00 PM - 9:00 PM</time>"
                + "<div class='location'>Downtown Arena</div>"
                + "<div class='region'>Raleigh</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("January 15, 2024");
        assertThat(description).contains("7:00 PM - 9:00 PM");
        assertThat(description).contains("Downtown Arena");
        assertThat(description).contains("Raleigh");
    }

    @Test
    void extractDescription_fromParagraphTag() {
        String html = "<div><p>This is the event description.</p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEqualTo("This is the event description.");
    }

    @Test
    void extractDescription_fromDescriptionClass() {
        String html = "<div><div class='event-description'>"
                + "Come join us for this exciting event!"
                + "</div></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEqualTo("Come join us for this exciting event!");
    }

    @Test
    void extractDescription_fromExcerptClass() {
        String html = "<div><div class='event-excerpt'>Event excerpt text</div></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEqualTo("Event excerpt text");
    }

    @Test
    void extractDescription_prefersBlockMetaOverParagraph() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'>Block meta description</div>"
                + "</div>"
                + "<p>Paragraph description</p>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("Block meta description");
        assertThat(description).doesNotContain("Paragraph description");
    }

    @Test
    void extractDescription_withNoDescription_returnsEmpty() {
        String html = "<div><span>No description here</span></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEmpty();
    }

    @Test
    void extractDescription_trimsWhitespace() {
        String html = "<div><p>  Whitespace test  </p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEqualTo("Whitespace test");
    }

    @Test
    void extractDescription_withEmptyParagraph_returnsEmpty() {
        String html = "<div><p></p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEmpty();
    }

    @Test
    void extractDescription_withOnlyWhitespaceInParagraph_returnsEmpty() {
        String html = "<div><p>   </p></div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEmpty();
    }

    @Test
    void extractDescription_fromBlockMetaWithOnlyTime() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<time>8:00 AM - 5:00 PM</time>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("8:00 AM - 5:00 PM");
    }

    @Test
    void extractDescription_fromBlockMetaWithOnlyLocation() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<div class='location'>Convention Center</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("Convention Center");
    }

    @Test
    void extractDescription_fromComplexRealWorldHTML() {
        String html = "<div class='event-card'>"
                + "<div class='event-content'>"
                + "<h3>Event Title</h3>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'>June 20-22, 2024</div>"
                + "<time>All Day</time>"
                + "<div class='location'>City Park</div>"
                + "<div class='region'>North Raleigh</div>"
                + "</div>"
                + "<p>Additional description text</p>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("June 20-22, 2024");
        assertThat(description).contains("All Day");
        assertThat(description).contains("City Park");
        assertThat(description).contains("North Raleigh");
    }

    @Test
    void extractDescription_withCaseInsensitiveClassName() {
        String html = "<div>"
                + "<div class='block-Meta-Info'>"
                + "<div class='DateInfo'>Test Date</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("Test Date");
    }

    @Test
    void extractDescription_withPartialClassMatch() {
        String html = "<div>"
                + "<div class='event-block-meta-container'>"
                + "<div class='event-dateInfo-wrapper'>Date Content</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("Date Content");
    }

    @Test
    void extractDescription_fallbackPrefersParagraphFirst() {
        String html = "<div>"
                + "<p>Paragraph text</p>"
                + "<div class='event-description'>Description class text</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEqualTo("Paragraph text");
    }

    @Test
    void extractDescription_withNestedBlockMeta() {
        String html = "<div>"
                + "<div class='wrapper'>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'>Nested date</div>"
                + "</div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).contains("Nested date");
    }

    @Test
    void extractDescription_withAllFieldsEmpty_returnsEmpty() {
        String html = "<div>"
                + "<div class='block-meta'>"
                + "<div class='dateInfo'></div>"
                + "<time></time>"
                + "<div class='location'></div>"
                + "</div>"
                + "</div>";
        Element eventCard = Jsoup.parse(html).body().child(0);

        String description = extractor.extractDescription(eventCard);

        assertThat(description).isEmpty();
    }
}
