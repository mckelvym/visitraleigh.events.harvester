package visitraleigh.events.webdriver;

import static java.util.Objects.requireNonNull;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading web pages with Selenium and parsing with JSoup.
 *
 * <p>This class handles the process of:
 * <ul>
 *   <li>Loading a URL with Selenium WebDriver</li>
 *   <li>Waiting for specific elements to be present (page fully loaded)</li>
 *   <li>Extracting the page source</li>
 *   <li>Parsing the HTML with JSoup for easier manipulation</li>
 *   <li>Optional debug output for troubleshooting</li>
 * </ul>
 */
public class PageLoader {

    private static final Logger LOG = LoggerFactory.getLogger(PageLoader.class);

    private final WebDriver driver;
    private final Duration timeout;
    private final String waitSelector;
    private final boolean debugMode;

    /**
     * Creates a new page loader with the specified configuration.
     *
     * @param driver The WebDriver instance to use for page loading
     * @param timeout The maximum time to wait for pages to load
     * @param waitSelector The CSS selector to wait for (indicates page loaded)
     * @param debugMode Whether to save debug HTML files
     */
    public PageLoader(
            WebDriver driver,
            Duration timeout,
            String waitSelector,
            boolean debugMode) {
        this.driver = requireNonNull(driver);
        this.timeout = requireNonNull(timeout);
        this.waitSelector = requireNonNull(waitSelector);
        this.debugMode = debugMode;
    }

    /**
     * Loads a page and waits for an element before parsing.
     *
     * <p>This method:
     * <ol>
     *   <li>Navigates to the URL using Selenium</li>
     *   <li>Waits for the wait selector to be present (with timeout)</li>
     *   <li>Extracts the page source HTML</li>
     *   <li>Optionally saves debug HTML file</li>
     *   <li>Parses the HTML with JSoup and returns the Document</li>
     * </ol>
     *
     * @param url The URL to load
     * @param pageNumber The page number (for debug file naming)
     * @return JSoup Document parsed from the page source
     * @throws IOException if saving debug file fails
     */
    public Document loadAndParse(String url, int pageNumber) throws IOException {
        final long startTime = System.currentTimeMillis();

        LOG.debug("Loading page: {}", url);
        driver.get(url);
        LOG.debug("Waiting for page to load...");

        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(waitSelector)));

        LOG.debug("Page loaded in {}ms", (System.currentTimeMillis() - startTime));

        String pageSource = driver.getPageSource();
        saveDebugPageIfNeeded(pageSource, pageNumber);

        return Jsoup.parse(requireNonNull(pageSource), url);
    }

    /**
     * Saves page source to a debug HTML file if debug mode is enabled.
     *
     * <p>Only saves the first page (page 1) to avoid creating too many files.
     *
     * @param pageSource The HTML source code to save
     * @param pageNumber The page number
     * @throws IOException if writing the file fails
     */
    private void saveDebugPageIfNeeded(String pageSource, int pageNumber)
            throws IOException {
        if (debugMode && pageNumber == 1) {
            try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream("debug-page.html"),
                                    StandardCharsets.UTF_8)))) {
                out.println(pageSource);
            }
            LOG.debug("Debug: Page source saved to debug-page.html");
            LOG.debug("Debug: Page source length: {} characters",
                    pageSource != null ? pageSource.length() : 0);
        }
    }
}
