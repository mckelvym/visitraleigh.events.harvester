package visitraleigh.events.scraper.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaginationParserTest {

    private PaginationParser parser;
    private static final String SELECTOR = "li.arrow.arrow-next.arrow-double";
    private static final Pattern PATTERN = Pattern.compile("page=(\\d+)");
    private static final int DEFAULT = 5;

    @BeforeEach
    void setUp() {
        parser = new PaginationParser(SELECTOR, PATTERN, DEFAULT);
    }

    @Test
    void getNumPages_withValidPagination_extractsPageNumber() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?page=10'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(10);
    }

    @Test
    void getNumPages_withNoPagination_returnsDefault() {
        String html = "<div>No pagination here</div>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }

    @Test
    void getNumPages_withEmptyHref_returnsDefault() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href=''>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }

    @Test
    void getNumPages_withNoChildren_returnsDefault() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'></li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }

    @Test
    void getNumPages_withNoMatchingPattern_returnsDefault() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?invalid=parameter'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }

    @Test
    void getNumPages_withSinglePage_extractsOne() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?page=1'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(1);
    }

    @Test
    void getNumPages_withLargePageNumber_extracts() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?page=999'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(999);
    }

    @Test
    void getNumPages_withMultipleParameters_extractsPageNumber() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?startDate=2024-01-01&page=7&endDate=2024-12-31'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(7);
    }

    @Test
    void getNumPages_withComplexHTML() {
        String html = "<div class='content'>"
                + "<div class='results'>"
                + "<ul class='pagination'>"
                + "<li class='arrow arrow-prev arrow-double'><a href='?page=1'>First</a></li>"
                + "<li class='arrow arrow-prev'><a href='?page=4'>Prev</a></li>"
                + "<li><a href='?page=3'>3</a></li>"
                + "<li><a href='?page=4'>4</a></li>"
                + "<li class='active'><a href='?page=5'>5</a></li>"
                + "<li><a href='?page=6'>6</a></li>"
                + "<li class='arrow arrow-next'><a href='?page=6'>Next</a></li>"
                + "<li class='arrow arrow-next arrow-double'><a href='?page=15'>Last</a></li>"
                + "</ul>"
                + "</div>"
                + "</div>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(15);
    }

    @Test
    void getNumPages_withInvalidNumberFormat_returnsDefault() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?page=abc'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }

    @Test
    void getNumPages_withZeroPages_extractsZero() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<a href='?page=0'>Last</a>"
                + "</li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(0);
    }

    @Test
    void getNumPages_withMultiplePaginationElements_usesFirst() {
        String html = "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'><a href='?page=3'>Last</a></li>"
                + "<li class='arrow arrow-next arrow-double'><a href='?page=5'>Last</a></li>"
                + "</ul>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(3);
    }

    @Test
    void getNumPages_withNestedStructure_returnsDefault() {
        String html = "<div class='wrapper'>"
                + "<ul class='pagination'>"
                + "<li class='arrow arrow-next arrow-double'>"
                + "<span><a href='?page=8'>Last</a></span>"
                + "</li>"
                + "</ul>"
                + "</div>";
        Document doc = Jsoup.parse(html);

        int numPages = parser.getNumPages(doc);

        assertThat(numPages).isEqualTo(DEFAULT);
    }
}
