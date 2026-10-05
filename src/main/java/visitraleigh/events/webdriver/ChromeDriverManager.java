package visitraleigh.events.webdriver;

import static java.util.Objects.requireNonNull;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.DISABLE_DEV_SHM;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.DISABLE_GPU;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.HEADLESS;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.NO_SANDBOX;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.USER_AGENT_PREFIX;
import static visitraleigh.events.webdriver.ChromeOptionsConstants.WINDOW_SIZE;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visitraleigh.events.config.ScraperConfiguration;

/**
 * Manages ChromeDriver for headless browsing.
 */
public class ChromeDriverManager implements WebDriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChromeDriverManager.class);
    private final WebDriver driver;

    /**
     * Creates a new ChromeDriverManager with the given configuration.
     *
     * @param config Scraper configuration
     * @throws NullPointerException if config is null
     */
    public ChromeDriverManager(ScraperConfiguration config) {
        requireNonNull(config, "config must not be null");
        ChromeOptions options = createChromeOptions(config);
        driver = new ChromeDriver(options);

        driver.manage().timeouts().pageLoadTimeout(config.getPageLoadTimeout());

        LOG.info("Chrome WebDriver initialized in headless mode");
    }

    private ChromeOptions createChromeOptions(ScraperConfiguration config) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(HEADLESS);
        options.addArguments(NO_SANDBOX);
        options.addArguments(DISABLE_DEV_SHM);
        options.addArguments(DISABLE_GPU);
        options.addArguments(WINDOW_SIZE);
        options.addArguments(USER_AGENT_PREFIX + config.getUserAgent());
        return options;
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Override
    public void quit() {
        if (driver != null) {
            driver.quit();
            LOG.info("Shutting down Chrome WebDriver");
        }
    }
}
