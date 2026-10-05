package visitraleigh.events.webdriver;

import static java.util.Objects.requireNonNull;

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
 */
public record PageLoader(WebDriver driver, Duration timeout) {

    private static final Logger LOG = LoggerFactory.getLogger(PageLoader.class);

    /**
     * Creates a new PageLoader.
     *
     * @param driver  the WebDriver to use
     * @param timeout the page load timeout
     */
    public PageLoader(final WebDriver driver, final Duration timeout) {
        this.driver = requireNonNull(driver);
        this.timeout = requireNonNull(timeout);
    }

    /**
     * Loads a page and waits for an element before parsing.
     *
     * @param url      The URL to load
     * @param selector css selector
     * @return JSoup Document parsed from the page source
     * @throws NullPointerException if url is null
     */
    public Document loadPage(String url, String selector) {
        requireNonNull(url, "url must not be null");
        requireNonNull(selector, "selector must not be null");
        LOG.info("Loading page: {}", url);
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));

        String pageSource = driver.getPageSource();
        return Jsoup.parse(requireNonNull(pageSource), url);
    }
}
