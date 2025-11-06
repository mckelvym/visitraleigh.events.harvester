package visitraleigh.events;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * RSS Feed Generator for Visit Raleigh Events
 */
public class RaleighEventsRSSGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(RaleighEventsRSSGenerator.class);

    private static final String BASE_URL = "https://www.visitraleigh.com/events/";
    private static final boolean DEBUG_MODE = false;
    private static final int DEFAULT_NUM_PAGES = 10;
    private static final int DAYS_INTO_FUTURE = getDaysIntoFuture();
    private static final int DROP_EVENTS_OLDER_THAN_DAYS = getDropEventsOlderThanDays();
    private static final Pattern NUM_PAGES_PATTERN = Pattern.compile("(?:^|[?&])page=(\\d+)");
    private static final String LAST_PAGE_LINK_ELEMENT = "li.arrow.arrow-next.arrow-double";

    private final Set<String> existingGuids;
    private final List<EventItem> newEvents;
    private final String rssFilePath;

    public RaleighEventsRSSGenerator(String rssFilePath) {
        this.rssFilePath = requireNonNull(rssFilePath);
        this.existingGuids = new HashSet<>();
        this.newEvents = new ArrayList<>();
    }

    private static int getDaysIntoFuture() {
        String envValue = System.getenv("DAYS_INTO_FUTURE");
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid DAYS_INTO_FUTURE value: {}, using default of 30", envValue);
            }
        }
        return 30;
    }

    private static int getDropEventsOlderThanDays() {
        String envValue = System.getenv("DROP_EVENTS_OLDER_THAN_DAYS");
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid DROP_EVENTS_OLDER_THAN_DAYS value: {}, using default of 30",
                        envValue);
            }
        }
        return 30;
    }

    private static String getEndDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return ZonedDateTime.now().plusDays(DAYS_INTO_FUTURE).format(formatter);
    }

    public static void main(String[] args) {
        // Bridge Java Util Logging to SLF4J for Selenium
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();

        if (args.length < 1) {
            LOG.error("<rss-file-path> is missing");
            System.exit(1);
        }

        String rssFilePath = args[0];
        RaleighEventsRSSGenerator generator = new RaleighEventsRSSGenerator(rssFilePath);

        try {
            generator.loadExistingFeed();
            generator.scrapeEvents();
            generator.generateRSSFeed();
            LOG.info("RSS feed generated successfully with {} new events",
                    generator.newEvents.size());
        } catch (Exception e) {
            LOG.error("Error generating RSS feed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void loadExistingFeed()
            throws ParserConfigurationException, IOException, SAXException {
        File rssFile = new File(rssFilePath);
        if (!rssFile.exists()) {
            LOG.info("No existing RSS feed found. Starting fresh.");
            return;
        }

        DocumentBuilderFactory factory = getDocumentBuilderFactory();

        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(rssFile);

        org.w3c.dom.NodeList guidNodes = doc.getElementsByTagName("guid");
        for (int i = 0; i < guidNodes.getLength(); i++) {
            String guid = guidNodes.item(i).getTextContent();
            existingGuids.add(guid);
        }

        LOG.info("Loaded {} existing event GUIDs", existingGuids.size());
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Disable external entity processing
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private void scrapeEvents()
            throws IOException {
        WebDriver driver = getWebDriver();

        try {
            int numPages = scrapeAllPages(driver);
            LOG.info("Successfully parsed {} new events across {} pages",
                    newEvents.size(), numPages);
        } finally {
            driver.quit();
        }
    }

    private int scrapeAllPages(WebDriver driver)
            throws IOException {
        int page = 1;
        int numPages = 1;
        String endDate = getEndDate();

        while (page <= numPages) {
            String url = BASE_URL + "?page=" + page + "&endDate=" + endDate;
            LOG.info("Scraping {}", url);

            Document doc = loadAndParsePage(driver, url, page);

            if (page == 1) {
                numPages = getNumPages(doc);
                LOG.info("There are {} pages to parse.", numPages);
            }

            scrapeEventsFromPage(doc);
            LOG.info("Successfully parsed {} new events so far (page {} of {})",
                    newEvents.size(), page, numPages);

            page++;
        }

        return numPages;
    }

    private Document loadAndParsePage(WebDriver driver, String url, int page)
            throws IOException {
        final long startTime = System.currentTimeMillis();
        driver.get(url);
        LOG.debug("Waiting for page to load...");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(LAST_PAGE_LINK_ELEMENT)));

        LOG.debug("Page loaded in {}ms", (System.currentTimeMillis() - startTime));

        String pageSource = driver.getPageSource();
        saveDebugPageIfNeeded(pageSource, page);

        return Jsoup.parse(requireNonNull(pageSource), url);
    }

    private void saveDebugPageIfNeeded(String pageSource, int page)
            throws IOException {
        if (DEBUG_MODE && page == 1) {
            try (PrintWriter out = new PrintWriter(new FileWriter("debug-page.html"))) {
                out.println(pageSource);
            }
            LOG.debug("Debug: Page source saved to debug-page.html");
            LOG.debug("Debug: Page source length: {} characters",
                    pageSource != null ? pageSource.length() : 0);
        }
    }

    private void scrapeEventsFromPage(Document doc) {
        Elements allLinks = doc.select("a[href*='/event/']");
        LOG.debug("Found {} links containing '/event/'", allLinks.size());

        Set<String> processedUrls = new HashSet<>();

        for (Element link : allLinks) {
            String href = link.attr("abs:href");

            if (shouldProcessEventLink(href, processedUrls)) {
                processedUrls.add(href);
                processEventLink(link);
            }
        }
    }

    private boolean shouldProcessEventLink(String href, Set<String> processedUrls) {
        if (processedUrls.contains(href)) {
            return false;
        }

        if (!href.contains("visitraleigh.com/event/")) {
            return false;
        }

        return href.matches(".*/event/[^/]+/\\d+/?$");
    }

    private void processEventLink(Element link) {
        EventItem event = parseEventFromLink(link);

        if (event != null) {
            if (!existingGuids.add(event.guid)) {
                LOG.info("Found existing event: {}", event.title);
            } else {
                newEvents.add(event);
                LOG.info("Found new event: {}", event.title);
            }
        }
    }

    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    private int getNumPages(Document doc) {
        Elements doubleArrow = doc.select(LAST_PAGE_LINK_ELEMENT);
        if (doubleArrow.isEmpty()) {
            return DEFAULT_NUM_PAGES;
        }

        String href = extractHrefFromPaginationElement(doubleArrow);
        if (href == null) {
            return DEFAULT_NUM_PAGES;
        }

        return parsePageNumberFromHref(href);
    }

    private String extractHrefFromPaginationElement(Elements doubleArrow) {
        Element first = doubleArrow.first();
        if (first == null) {
            return null;
        }

        Elements children = first.children();
        if (children.isEmpty()) {
            return null;
        }

        Element firstChild = children.first();
        if (firstChild == null) {
            return null;
        }

        String href = firstChild.attr("href");
        return href.isEmpty() ? null : href;
    }

    private int parsePageNumberFromHref(String href) {
        Matcher matcher = NUM_PAGES_PATTERN.matcher(href);

        if (!matcher.find()) {
            return DEFAULT_NUM_PAGES;
        }
        String pageNumber = matcher.group(1);
        try {
            return Integer.parseInt(pageNumber);
        } catch (NumberFormatException e) {
            LOG.error("Failed to parse number of pages", e);
            return DEFAULT_NUM_PAGES;
        }
    }

    private EventItem parseEventFromLink(Element linkElement) {
        try {
            String eventUri = linkElement.attr("abs:href");
            logDebug("Parsing link: {}", eventUri);

            int id = extractEventId(eventUri);
            Element eventCard = findEventCardContainer(linkElement);

            String title = extractTitle(eventCard);
            if (title == null) {
                return null;
            }

            logDebug("Title: {}", title);

            String dateStr = extractDate(eventCard);
            String description = extractDescription(eventCard);
            String imageUrl = extractImageUrl(eventCard);
            String fullTitle = buildFullTitle(title, dateStr);

            return new EventItem(id, eventUri, fullTitle, description, eventUri, imageUrl, dateStr);

        } catch (Exception e) {
            logDebug("Error parsing link: {}", e.getMessage(), e);
            return null;
        }
    }

    private int extractEventId(String eventUri) {
        final List<String> strings = Splitter.on("/").omitEmptyStrings().splitToList(eventUri);
        final String lastElement = strings.get(strings.size() - 1);
        return getEventId(lastElement);
    }

    private Element findEventCardContainer(Element linkElement) {
        Element eventCard = linkElement;
        for (int i = 0; i < 10; i++) {
            Element parent = eventCard.parent();
            if (parent == null) {
                break;
            }

            if (isEventCardContainer(parent)) {
                return parent;
            }
            eventCard = parent;
        }
        return eventCard;
    }

    private boolean isEventCardContainer(Element element) {
        String className = element.className().toLowerCase();
        String tagName = element.tagName().toLowerCase();

        return className.contains("event") || className.contains("card") ||
                className.contains("result") || className.contains("listing") ||
                tagName.equals("article") || className.contains("item");
    }

    private String extractTitle(Element eventCard) {
        String title = extractTitleFromHeadings(eventCard);

        if (title.length() < 3) {
            title = extractTitleFromClass(eventCard);
        }

        if (title.length() < 3) {
            title = extractTitleFromLinks(eventCard);
        }

        if (title.length() < 3) {
            title = extractTitleFromImage(eventCard);
        }

        if (title.length() < 3) {
            title = extractTitleFromAriaLabel(eventCard);
        }

        if (title.length() < 3) {
            logEventCardDebugInfo(eventCard);
            return null;
        }

        return title;
    }

    private String extractTitleFromHeadings(Element eventCard) {
        Element heading = eventCard.selectFirst("h1, h2, h3, h4, h5, h6");
        return heading != null ? heading.text().trim() : "";
    }

    private String extractTitleFromClass(Element eventCard) {
        Element titleElem = eventCard.selectFirst("[class*='title'], [class*='Title'], " +
                "[class*='name'], [class*='Name']");
        return titleElem != null ? titleElem.text().trim() : "";
    }

    private String extractTitleFromLinks(Element eventCard) {
        Elements links = eventCard.select("a[href*='/event/']");
        for (Element link : links) {
            String linkText = link.text().trim();
            if (linkText.length() > 3) {
                return linkText;
            }
        }
        return "";
    }

    private String extractTitleFromImage(Element eventCard) {
        Element img = eventCard.selectFirst("img[alt]");
        if (img != null) {
            String alt = img.attr("alt").trim();
            if (alt.length() > 3) {
                return alt;
            }
        }
        return "";
    }

    private String extractTitleFromAriaLabel(Element eventCard) {
        Elements linksWithAria = eventCard.select("a[aria-label]");
        for (Element link : linksWithAria) {
            String ariaLabel = link.attr("aria-label").trim();
            if (ariaLabel.length() > 3) {
                return ariaLabel;
            }
        }
        return "";
    }

    private void logEventCardDebugInfo(Element eventCard) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Could not extract title");
            String eventCardHtml = eventCard.html();
            LOG.trace("Event card HTML (first 300 chars): {}",
                    eventCardHtml.substring(0, Math.min(300, eventCardHtml.length())));
        }
    }

    private String extractDate(Element eventCard) {
        Element dateElement = eventCard.selectFirst("time, [class*='date'], [class*='Date']");
        return dateElement != null ? dateElement.text().trim() : "";
    }

    private String extractDescription(Element eventCard) {
        String description = extractDescriptionFromBlockMeta(eventCard);

        if (description.isEmpty()) {
            description = extractDescriptionFromFallback(eventCard);
        }

        return description;
    }

    private String extractDescriptionFromBlockMeta(Element eventCard) {
        Element blockMeta = eventCard.selectFirst("div.block-meta, [class*='block-meta']");
        if (blockMeta == null) {
            return "";
        }

        StringBuilder descBuilder = new StringBuilder();
        UnaryOperator<String> liWrap = str -> "<br/>" + str;

        appendTextIfPresent(descBuilder, blockMeta, "[class*='dateInfo'], [class*='date-info']",
                liWrap);
        appendTextIfPresent(descBuilder, blockMeta, "[class*='times'], time", liWrap);
        appendTextIfPresent(descBuilder, blockMeta, "[class*='location']", liWrap);
        appendTextIfPresent(descBuilder, blockMeta, "[class*='region']", liWrap);

        return descBuilder.toString().trim();
    }

    private void appendTextIfPresent(StringBuilder builder, Element parent, String selector,
                                     UnaryOperator<String> wrapper) {
        Element element = parent.selectFirst(selector);
        if (element != null) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                builder.append(wrapper.apply(text));
                if (!selector.contains("region")) {
                    builder.append(" ");
                }
            }
        }
    }

    private String extractDescriptionFromFallback(Element eventCard) {
        Element descElement = eventCard.selectFirst("p, [class*='description'], " +
                "[class*='excerpt']");
        return descElement != null ? descElement.text().trim() : "";
    }

    private String extractImageUrl(Element eventCard) {
        Element imgElement = eventCard.selectFirst("img[src]");
        if (imgElement != null) {
            String src = imgElement.attr("abs:src");
            if (!src.contains("icon") && !src.contains("logo") && src.length() > 20) {
                return src;
            }
        }
        return "";
    }

    private String buildFullTitle(String title, String dateStr) {
        if (!dateStr.isEmpty()) {
            return title + " (" + dateStr + ")";
        }
        return title;
    }

    private void logDebug(String message, Object... args) {
        if (DEBUG_MODE) {
            LOG.debug(message, args);
        }
    }

    private static int getEventId(String lastElement) {
        int id = -1;
        try {
            id = Integer.parseInt(lastElement);
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse value as int: {}", lastElement);
        }
        return id;
    }

    private void generateRSSFeed()
            throws ParserConfigurationException, TransformerException,
            IOException, SAXException {
        DocumentBuilderFactory factory = getDocumentBuilderFactory();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.newDocument();

        org.w3c.dom.Element rss = doc.createElement("rss");
        rss.setAttribute("version", "2.0");
        doc.appendChild(rss);

        org.w3c.dom.Element channel = doc.createElement("channel");
        rss.appendChild(channel);

        addElement(doc, channel, "title", "Visit Raleigh Events");
        addElement(doc, channel, "link", BASE_URL);
        addElement(doc, channel, "description", "Events from Visit Raleigh");
        addElement(doc, channel, "language", "en-us");
        addElement(doc, channel, "lastBuildDate",
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        newEvents.sort(Comparator.reverseOrder());
        for (EventItem event : newEvents) {
            addEventItem(doc, channel, event);
        }

        int droppedEventsCount = 0;
        if (new File(rssFilePath).exists()) {
            org.w3c.dom.Document oldDoc = builder.parse(new File(rssFilePath));
            oldDoc.getDocumentElement().normalize();
            org.w3c.dom.NodeList oldItems = oldDoc.getElementsByTagName("item");

            ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(DROP_EVENTS_OLDER_THAN_DAYS);
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

            for (int i = 0; i < oldItems.getLength(); i++) {
                org.w3c.dom.Node oldItem = oldItems.item(i);
                ZonedDateTime pubDate = extractPubDateFromItem(oldItem, formatter);

                if (pubDate != null && pubDate.isBefore(cutoffDate)) {
                    droppedEventsCount++;
                    continue;
                }

                org.w3c.dom.Node importedNode = doc.importNode(oldItem, true);
                removeWhitespaceNodes(importedNode);
                channel.appendChild(importedNode);
            }
        }

        logFeedStatistics(doc, droppedEventsCount);

        TransformerFactory transformerFactory = getTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(rssFilePath));
        transformer.transform(source, result);

        LOG.info("RSS feed written to: {}", rssFilePath);
    }

    private void logFeedStatistics(org.w3c.dom.Document doc, int droppedEventsCount) {
        org.w3c.dom.NodeList items = doc.getElementsByTagName("item");
        int totalEvents = items.getLength();

        if (totalEvents == 0) {
            LOG.info("RSS feed contains 0 events");
            if (droppedEventsCount > 0) {
                LOG.info("Dropped {} old events (older than {} days)",
                        droppedEventsCount, DROP_EVENTS_OLDER_THAN_DAYS);
            }
            return;
        }

        ZonedDateTime oldestDate = findLastPubDate(items).orElse(null);
        logEventStatistics(totalEvents, oldestDate, droppedEventsCount);
    }

    private Optional<ZonedDateTime> findLastPubDate(org.w3c.dom.NodeList items) {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        if (items.getLength() <= 0) {
            return Optional.empty();
        }

        org.w3c.dom.Node item = items.item(items.getLength() - 1);
        return Optional.ofNullable(extractPubDateFromItem(item, formatter));
    }

    private ZonedDateTime extractPubDateFromItem(org.w3c.dom.Node item,
                                                  DateTimeFormatter formatter) {
        org.w3c.dom.NodeList children = item.getChildNodes();

        for (int j = 0; j < children.getLength(); j++) {
            org.w3c.dom.Node child = children.item(j);
            if ("pubDate".equals(child.getNodeName())) {
                try {
                    String pubDateStr = child.getTextContent();
                    return ZonedDateTime.parse(pubDateStr, formatter);
                } catch (Exception e) {
                    LOG.debug("Failed to parse pubDate: {}", child.getTextContent(), e);
                    return null;
                }
            }
        }

        return null;
    }

    private void logEventStatistics(int totalEvents, ZonedDateTime oldestDate,
                                     int droppedEventsCount) {
        if (oldestDate != null) {
            long daysSinceOldest = Duration.between(oldestDate, ZonedDateTime.now()).toDays();
            LOG.info("RSS feed contains {} total events, oldest entry is {} days old",
                    totalEvents, daysSinceOldest);
        } else {
            LOG.info("RSS feed contains {} total events", totalEvents);
        }

        if (droppedEventsCount > 0) {
            LOG.info("Dropped {} old events (older than {} days)",
                    droppedEventsCount, DROP_EVENTS_OLDER_THAN_DAYS);
        }
    }

    private static TransformerFactory getTransformerFactory() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Disable external entity processing
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return transformerFactory;
    }

    private void removeWhitespaceNodes(org.w3c.dom.Node node) {
        java.util.Deque<org.w3c.dom.Node> stack = new java.util.ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            org.w3c.dom.Node current = stack.pop();
            org.w3c.dom.NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                org.w3c.dom.Node child = children.item(i);
                if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    private void addEventItem(org.w3c.dom.Document doc, org.w3c.dom.Element channel,
                              EventItem event) {
        org.w3c.dom.Element item = doc.createElement("item");
        channel.appendChild(item);

        addElement(doc, item, "title", event.title);
        addElement(doc, item, "link", event.link);
        addElement(doc, item, "description", event.description);
        addElement(doc, item, "guid", event.guid);
        addElement(doc, item, "pubDate",
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        if (!event.imageUrl.isEmpty()) {
            org.w3c.dom.Element enclosure = doc.createElement("enclosure");
            enclosure.setAttribute("url", event.imageUrl);
            enclosure.setAttribute("type", "image/jpeg");
            item.appendChild(enclosure);
        }
    }

    private void addElement(org.w3c.dom.Document doc, org.w3c.dom.Element parent,
                            String tagName, String textContent) {
        org.w3c.dom.Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private record EventItem(int id, String guid, String title, String description, String link,
                             String imageUrl,
                             String dateStr) implements Comparable<EventItem> {
        @Override
        public int compareTo(EventItem o) {
            return Integer.compare(this.id, o.id);
        }
    }
}
