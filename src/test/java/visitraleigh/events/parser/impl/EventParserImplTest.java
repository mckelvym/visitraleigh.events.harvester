package visitraleigh.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import visitraleigh.events.domain.EventItem;

/**
 * Comprehensive tests for EventParserImpl.
 * Tests integration of all extractors and event parsing logic.
 */
class EventParserImplTest {

    private EventParserImpl parser;

    @BeforeEach
    void setUp() {
        parser = new EventParserImpl();
    }

    @Test
    void parseEvent_withCompleteEvent_returnsEventItem() {
        String html = """
            <a href="https://www.visitraleigh.com/event/music-festival/12345/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Music Festival</h3>
                    <div class="event-date">December 15, 2025</div>
                    <div class="event-description">Annual music celebration</div>
                    <img class="event-image" src="https://example.com/festival.jpg"/>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.id()).isEqualTo("12345");
        assertThat(event.title()).contains("Music Festival");
        assertThat(event.title()).contains("December 15, 2025");
        assertThat(event.link()).isEqualTo("https://www.visitraleigh.com/event/music-festival/12345/");
        assertThat(event.description()).isEqualTo("Annual music celebration");
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 15));
        assertThat(event.eventDateEnd()).isNull();
        assertThat(event.imageUrl()).isEqualTo("https://example.com/festival.jpg");
        assertThat(event.location()).isNull();
    }

    @Test
    void parseEvent_withMissingOptionalFields_returnsEventWithDefaults() {
        String html = """
            <a href="https://www.visitraleigh.com/event/simple/789/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Simple Event</h3>
                    <div class="event-date">December 20, 2025</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).contains("Simple Event");
        assertThat(event.description()).isEmpty();
        assertThat(event.imageUrl()).isEmpty();
    }


    @Test
    void parseEvent_extractsIdFromUrl() {
        String html = """
            <a href="https://www.visitraleigh.com/event/unique-event/99999/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Unique Event</h3>
                    <div class="event-date">December 15, 2025</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("99999");
    }

    @Test
    void parseEvent_titleIncludesDate() {
        String html = """
            <a href="https://www.visitraleigh.com/event/concert/555/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Concert Night</h3>
                    <div class="event-date">December 31, 2025</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).contains("Concert Night");
        assertThat(event.title()).contains("December 31, 2025");
    }


    @Test
    void parseEvent_withWhitespaceInFields_trimsWhitespace() {
        String html = """
            <a href="https://www.visitraleigh.com/event/test/123/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">  Test Event  </h3>
                    <div class="event-date">  December 15, 2025  </div>
                    <div class="event-description">  Great event  </div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).contains("Test Event");
        assertThat(event.description()).isEqualTo("Great event");
    }

    @Test
    void parseEvent_withRelativeImageUrl_convertsToAbsolute() {
        String html = """
            <a href="https://www.visitraleigh.com/event/test/123/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Test Event</h3>
                    <div class="event-date">December 15, 2025</div>
                    <img class="event-image" src="/images/event.jpg"/>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html, "https://www.visitraleigh.com/").selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        assertThat(result.get().imageUrl()).startsWith("https://");
    }

    @Test
    void parseEvent_withMultipleDateFormats_parsesCorrectly() {
        String[][] testCases = {
            {"December 15, 2025"},
            {"Jan 1, 2026"},
            {"March 20, 2025"}
        };

        for (String[] testCase : testCases) {
            String dateString = testCase[0];

            String html = String.format("""
                <a href="https://www.visitraleigh.com/event/test/123/" class="event-link">
                    <div class="event-card">
                        <h3 class="event-title">Test Event</h3>
                        <div class="event-date">%s</div>
                    </div>
                </a>
                """, dateString);
            Element element = Jsoup.parse(html).selectFirst("a.event-link");

            Optional<EventItem> result = parser.parseEvent(element);

            assertThat(result).as("Date string: " + dateString).isPresent();
            assertThat(result.get().eventDateStart()).isNotNull();
        }
    }

    @Test
    void parseEvent_withEventCardFinder_findsEventCard() {
        String html = """
            <a href="https://www.visitraleigh.com/event/test/123/" class="event-link">
                <div class="other-wrapper">
                    <div class="event-card">
                        <h3 class="event-title">Nested Event</h3>
                        <div class="event-date">December 15, 2025</div>
                    </div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        assertThat(result.get().title()).contains("Nested Event");
    }

    @Test
    void parseEvent_withDatesVaryBetween_parsesDateRange() {
        String html = """
            <a href="https://www.visitraleigh.com/event/festival/456/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Art Festival</h3>
                    <div class="event-date">Dates vary between January 3, 2026 - January 4, 2026</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 3));
        assertThat(event.eventDateEnd()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 4));
        assertThat(event.title()).contains("Art Festival");
    }

    @Test
    void parseEvent_withRecurringWeeklyUntilDate_parsesDateRange() {
        String html = """
            <a href="https://www.visitraleigh.com/event/class/789/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Yoga Class</h3>
                    <div class="event-date">Recurring weekly on Sunday until December 27, 2026</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.now());
        assertThat(event.eventDateEnd()).isEqualTo(LocalDate.of(2026, Month.DECEMBER, 27));
        assertThat(event.title()).contains("Yoga Class");
    }

    @Test
    void parseEvent_withRecurringWeeklyNoEndDate_usesTodayAsStart() {
        String html = """
            <a href="https://www.visitraleigh.com/event/meeting/321/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Weekly Meeting</h3>
                    <div class="event-date">Recurring weekly on Sunday</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.now());
        assertThat(event.eventDateEnd()).isNull();
        assertThat(event.title()).contains("Weekly Meeting");
    }

    @Test
    void parseEvent_withRecurringMonthly_usesTodayAsStart() {
        String html = """
            <a href="https://www.visitraleigh.com/event/gathering/654/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Monthly Gathering</h3>
                    <div class="event-date">Recurring monthly on the 1st Sunday</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.now());
        assertThat(event.eventDateEnd()).isNull();
        assertThat(event.title()).contains("Monthly Gathering");
    }

    @Test
    void parseEvent_withRecurringMultipleDaysUntilDate_parsesEndDate() {
        String html = """
            <a href="https://www.visitraleigh.com/event/workshop/987/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Workshop Series</h3>
                    <div class="event-date">Recurring weekly on Sunday, Friday until January 2, 2028</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.now());
        assertThat(event.eventDateEnd()).isEqualTo(LocalDate.of(2028, Month.JANUARY, 2));
        assertThat(event.title()).contains("Workshop Series");
    }

    @Test
    void parseEvent_withDatesVaryBetweenLongerRange_parsesCorrectly() {
        String html = """
            <a href="https://www.visitraleigh.com/event/expo/111/" class="event-link">
                <div class="event-card">
                    <h3 class="event-title">Winter Expo</h3>
                    <div class="event-date">Dates vary between December 5, 2025 - January 4, 2026</div>
                </div>
            </a>
            """;
        Element element = Jsoup.parse(html).selectFirst("a.event-link");

        Optional<EventItem> result = parser.parseEvent(element);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.eventDateStart()).isEqualTo(LocalDate.of(2025, Month.DECEMBER, 5));
        assertThat(event.eventDateEnd()).isEqualTo(LocalDate.of(2026, Month.JANUARY, 4));
        assertThat(event.title()).contains("Winter Expo");
    }
}
