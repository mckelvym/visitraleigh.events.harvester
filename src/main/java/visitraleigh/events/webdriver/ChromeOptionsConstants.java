package visitraleigh.events.webdriver;

/**
 * Constants for Chrome WebDriver options and arguments.
 *
 * <p>This class centralizes all Chrome-specific command-line arguments
 * used when configuring the headless Chrome browser for web scraping.
 */
public final class ChromeOptionsConstants {

    /**
     * Enables the new headless mode in Chrome.
     * The new headless mode provides better compatibility and performance.
     */
    public static final String HEADLESS = "--headless=new";

    /**
     * Disables GPU hardware acceleration.
     * Required for headless mode and helps avoid crashes in containerized environments.
     */
    public static final String DISABLE_GPU = "--disable-gpu";

    /**
     * Disables Chrome's sandbox security feature.
     * Often required in Docker containers where sandboxing may not work properly.
     */
    public static final String NO_SANDBOX = "--no-sandbox";

    /**
     * Disables /dev/shm usage.
     * Prevents crashes in Docker environments with limited shared memory.
     */
    public static final String DISABLE_DEV_SHM = "--disable-dev-shm-usage";

    /**
     * Prefix for window size argument.
     * Followed by "{width},{height}" format.
     */
    public static final String WINDOW_SIZE_PREFIX = "--window-size=";

    /**
     * Prefix for user agent argument.
     * Followed by the custom user agent string.
     */
    public static final String USER_AGENT_PREFIX = "--user-agent=";

    private ChromeOptionsConstants() {
        // Utility class - prevent instantiation
    }
}
