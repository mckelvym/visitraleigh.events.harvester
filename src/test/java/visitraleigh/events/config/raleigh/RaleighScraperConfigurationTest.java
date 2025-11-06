package visitraleigh.events.config.raleigh;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RaleighScraperConfiguration.
 */
class RaleighScraperConfigurationTest {

    private RaleighScraperConfiguration config;

    @BeforeEach
    void setUp() {
        config = new RaleighScraperConfiguration();
    }

    @Test
    void testGetBaseUrl() {
        assertThat(config.getBaseUrl())
                .isEqualTo("https://www.visitraleigh.com/events/");
    }

    @Test
    void testIsDebugMode() {
        assertThat(config.isDebugMode()).isFalse();
    }

    @Test
    void testGetDefaultNumPages() {
        assertThat(config.getDefaultNumPages()).isEqualTo(10);
    }

    @Test
    void testGetDaysIntoFuture() {
        // Should be 30 by default (unless env var is set)
        assertThat(config.getDaysIntoFuture()).isGreaterThan(0);
    }

    @Test
    void testGetDropEventsOlderThanDays() {
        // Should be 30 by default (unless env var is set)
        assertThat(config.getDropEventsOlderThanDays()).isGreaterThan(0);
    }

    @Test
    void testGetLastPageLinkSelector() {
        assertThat(config.getLastPageLinkSelector())
                .isEqualTo("li.arrow.arrow-next.arrow-double");
    }

    @Test
    void testGetNumPagesPattern() {
        assertThat(config.getNumPagesPattern()).isNotNull();
        assertThat(config.getNumPagesPattern().pattern())
                .contains("page=");
    }

    @Test
    void testGetEventUrlPattern() {
        assertThat(config.getEventUrlPattern()).isNotNull();
        assertThat(config.getEventUrlPattern().pattern())
                .contains("/event/");
    }

    @Test
    void testGetPageLoadTimeout() {
        assertThat(config.getPageLoadTimeout())
                .isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void testGetUserAgent() {
        assertThat(config.getUserAgent())
                .contains("Mozilla")
                .contains("Chrome");
    }

    @Test
    void testGetWindowSize() {
        assertThat(config.getWindowSize())
                .isEqualTo("1920,1080");
    }

    @Test
    void testGetEndDate() {
        String endDate = config.getEndDate();
        assertThat(endDate)
                .matches("\\d{2}/\\d{2}/\\d{4}")
                .isNotNull();
    }

    @Test
    void testEventUrlPatternMatches() {
        assertThat(config.getEventUrlPattern().matcher("/event/sample-event/12345/").matches())
                .isTrue();
        assertThat(config.getEventUrlPattern().matcher("/event/another/67890").matches())
                .isTrue();
        assertThat(config.getEventUrlPattern().matcher("/not-an-event/").matches())
                .isFalse();
    }

    @Test
    void testNumPagesPatternMatches() {
        assertThat(config.getNumPagesPattern().matcher("?page=5").find())
                .isTrue();
        assertThat(config.getNumPagesPattern().matcher("page=10").find())
                .isTrue();
        assertThat(config.getNumPagesPattern().matcher("&page=15").find())
                .isTrue();
    }
}
