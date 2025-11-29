package visitraleigh.events.parser.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageExtractorTest {

    private ImageExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ImageExtractor();
    }

    @Test
    void extractImageUrl_fromImageElement() {
        String html = "<div><img src='https://example.com/event-image.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/event-image.jpg");
    }

    @Test
    void extractImageUrl_convertsRelativeToAbsolute() {
        String html = "<div><img src='/images/event.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).startsWith("https://example.com");
        assertThat(imageUrl).contains("images/event.jpg");
    }

    @Test
    void extractImageUrl_filtersOutIcon() {
        String html = "<div><img src='https://example.com/icon-small.png'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_filtersOutLogo() {
        String html = "<div><img src='https://example.com/company-logo.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_filtersOutShortUrl() {
        String html = "<div><img src='https://ex.co/a.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://ex.co");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_withNoImage_returnsEmpty() {
        String html = "<div><p>No image here</p></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_withImageWithoutSrc_returnsEmpty() {
        String html = "<div><img alt='No src attribute'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_selectsFirstImage() {
        String html = "<div>"
                + "<img src='https://example.com/first-image-url-is-long-enough.jpg'/>"
                + "<img src='https://example.com/second-image-url-is-long-enough.jpg'/>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/first-image-url-is-long-enough.jpg");
    }

    @Test
    void extractImageUrl_withNestedImage() {
        String html = "<div><div class='wrapper'>"
                + "<img src='https://example.com/nested-event-image.jpg'/>"
                + "</div></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/nested-event-image.jpg");
    }

    @Test
    void extractImageUrl_withComplexHTML() {
        String html = "<div class='event-card'>"
                + "<div class='event-image-wrapper'>"
                + "<img src='https://example.com/events/summer-festival-2024.jpg' "
                + "alt='Summer Festival'/>"
                + "</div>"
                + "<div class='event-content'>"
                + "<h3>Summer Festival</h3>"
                + "</div>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/events/summer-festival-2024.jpg");
    }

    @Test
    void extractImageUrl_withIconInUpperCase_notFiltered() {
        String html = "<div><img src='https://example.com/ICON-image.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/ICON-image.jpg");
    }

    @Test
    void extractImageUrl_withLogoInUpperCase_notFiltered() {
        String html = "<div><img src='https://example.com/company-LOGO.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/company-LOGO.jpg");
    }

    @Test
    void extractImageUrl_withExactly20Characters_returnsEmpty() {
        String html = "<div><img src='https://x.co/12.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://x.co");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }

    @Test
    void extractImageUrl_withLongEnoughUrl_returnsUrl() {
        String html = "<div><img src='https://example.com/long-enough-image.jpg'/></div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEqualTo("https://example.com/long-enough-image.jpg");
    }

    @Test
    void extractImageUrl_skipsFirstIfIcon_returnsSecond() {
        String html = "<div>"
                + "<img src='https://example.com/icon.png'/>"
                + "<img src='https://example.com/valid-event-image-url.jpg'/>"
                + "</div>";
        Document doc = Jsoup.parse(html, "https://example.com");
        Element eventCard = doc.body().child(0);

        String imageUrl = extractor.extractImageUrl(eventCard);

        assertThat(imageUrl).isEmpty();
    }
}
