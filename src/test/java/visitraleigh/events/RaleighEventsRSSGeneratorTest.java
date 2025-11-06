package visitraleigh.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

/**
 * Unit tests for RaleighEventsRSSGenerator.
 *
 * <p>These tests focus on testing individual methods and logic without
 * requiring actual web scraping.
 */
class RaleighEventsRSSGeneratorTest {

    @TempDir
    Path tempDir;

    private RaleighEventsRSSGenerator generator;
    private File testRssFile;

    @BeforeEach
    void setUp() throws IOException {
        testRssFile = tempDir.resolve("test-events.xml").toFile();
        generator = new RaleighEventsRSSGenerator(testRssFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        if (testRssFile != null && testRssFile.exists()) {
            testRssFile.delete();
        }
    }

    @Test
    void testConstructorRequiresNonNullPath() {
        assertThatThrownBy(() -> new RaleighEventsRSSGenerator(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testLoadExistingFeed_WhenFileDoesNotExist_ShouldNotThrow()
            throws ParserConfigurationException, IOException, SAXException {
        // Given: No RSS file exists
        assertThat(testRssFile).doesNotExist();

        // When: Loading existing feed
        java.lang.reflect.Method method;
        try {
            method = RaleighEventsRSSGenerator.class
                    .getDeclaredMethod("loadExistingFeed");
            method.setAccessible(true);
            method.invoke(generator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke loadExistingFeed", e);
        }

        // Then: Should complete without error
        // (No exception thrown)
    }

    @Test
    void testLoadExistingFeed_WithValidRssFile_ShouldLoadGuids() throws Exception {
        // Given: An existing RSS file with GUIDs
        String sampleRss = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <title>Test Events</title>
                        <link>https://example.com/</link>
                        <item>
                            <title>Event 1</title>
                            <guid>/event/test-1/12345/</guid>
                        </item>
                        <item>
                            <title>Event 2</title>
                            <guid>/event/test-2/67890/</guid>
                        </item>
                    </channel>
                </rss>
                """;
        Files.writeString(testRssFile.toPath(), sampleRss);

        // When: Loading existing feed
        java.lang.reflect.Method method = RaleighEventsRSSGenerator.class
                .getDeclaredMethod("loadExistingFeed");
        method.setAccessible(true);
        method.invoke(generator);

        // Then: GUIDs should be loaded
        java.lang.reflect.Field existingGuidsField = RaleighEventsRSSGenerator.class
                .getDeclaredField("existingGuids");
        existingGuidsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Set<String> existingGuids =
                (java.util.Set<String>) existingGuidsField.get(generator);

        assertThat(existingGuids)
                .hasSize(2)
                .contains("/event/test-1/12345/", "/event/test-2/67890/");
    }
}
