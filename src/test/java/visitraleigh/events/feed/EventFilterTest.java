package visitraleigh.events.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilderFactory;
import visitraleigh.events.config.impl.ScraperConfigurationImpl;
import visitraleigh.events.domain.EventItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class EventFilterTest {

    private static final int RETENTION_DAYS =
        new ScraperConfigurationImpl().getRetentionDays();
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate EXPIRED = TODAY.minusDays(RETENTION_DAYS + 1);
    private static final LocalDate CUTOFF = TODAY.minusDays(RETENTION_DAYS);

    private Document doc;
    private EventFilter filter;

    private static EventItem event(final LocalDate start, final LocalDate end) {
        return new EventItem("https://example.com/e", "Title", "https://example.com/e", null,
            start, end, null, null);
    }

    private static String rfc1123(final LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private Element item(final String... tagsAndValues) {
        final Element item = doc.createElement("item");
        for (int i = 0; i < tagsAndValues.length; i += 2) {
            final Element child = doc.createElement(tagsAndValues[i]);
            child.setTextContent(tagsAndValues[i + 1]);
            item.appendChild(child);
        }
        return item;
    }

    @BeforeEach
    void setUp() throws Exception {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        filter = new EventFilter(new ScraperConfigurationImpl());
    }

    @Test
    void constructorRejectsNullConfig() {
        assertThatThrownBy(() -> new EventFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldKeepEventWithinRetention() {
        assertThat(filter.shouldKeep(event(TODAY, null))).isTrue();
        assertThat(filter.shouldKeep(event(CUTOFF, null))).isTrue();
        assertThat(filter.shouldKeep(event(TODAY.plusDays(30), null))).isTrue();
    }

    @Test
    void shouldDropEventPastRetention() {
        assertThat(filter.shouldKeep(event(EXPIRED, null))).isFalse();
    }

    @Test
    void shouldKeepMultiDayEventUntilItsEndDatePasses() {
        assertThat(filter.shouldKeep(event(EXPIRED.minusDays(5), TODAY))).isTrue();
        assertThat(filter.shouldKeep(event(EXPIRED.minusDays(5), EXPIRED))).isFalse();
    }

    @Test
    void shouldKeepItemUsesEndDateBeforeStartDate() {
        assertThat(filter.shouldKeep(item(RssElementNames.EV_STARTDATE, EXPIRED.toString(),
            RssElementNames.EV_ENDDATE, TODAY.toString()))).isTrue();
        assertThat(filter.shouldKeep(item(RssElementNames.EV_STARTDATE, TODAY.toString(),
            RssElementNames.EV_ENDDATE, EXPIRED.toString()))).isFalse();
    }

    @Test
    void shouldKeepItemUsesStartDate() {
        assertThat(filter.shouldKeep(item(RssElementNames.EV_STARTDATE, CUTOFF.toString())))
            .isTrue();
        assertThat(filter.shouldKeep(item(RssElementNames.EV_STARTDATE, EXPIRED.toString())))
            .isFalse();
    }

    @Test
    void shouldKeepItemFallsBackToPubDateForLegacyItems() {
        assertThat(filter.shouldKeep(item(RssElementNames.PUB_DATE, rfc1123(TODAY)))).isTrue();
        assertThat(filter.shouldKeep(item(RssElementNames.PUB_DATE, rfc1123(EXPIRED)))).isFalse();
    }

    @Test
    void shouldKeepItemPrefersEventDateOverPubDate() {
        assertThat(filter.shouldKeep(item(RssElementNames.PUB_DATE, rfc1123(EXPIRED),
            RssElementNames.EV_STARTDATE, TODAY.toString()))).isTrue();
    }

    @Test
    void shouldKeepItemWithoutParseableDate() {
        assertThat(filter.shouldKeep(item())).isTrue();
        assertThat(filter.shouldKeep(item(RssElementNames.EV_STARTDATE, "not a date"))).isTrue();
        assertThat(filter.shouldKeep(item(RssElementNames.PUB_DATE, "garbage"))).isTrue();
    }

    @Test
    void shouldKeepRejectsNull() {
        assertThatThrownBy(() -> filter.shouldKeep((Element) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> filter.shouldKeep((EventItem) null))
            .isInstanceOf(NullPointerException.class);
    }
}
