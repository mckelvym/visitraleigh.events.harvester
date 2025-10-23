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
import java.util.Set;
import java.util.function.UnaryOperator;
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

    private static final Logger logger = LoggerFactory.getLogger(RaleighEventsRSSGenerator.class);

    private static final String BASE_URL = "https://www.visitraleigh.com/events/";
    private static final boolean DEBUG_MODE = false;
    private static final int DEFAULT_NUM_PAGES = 10;

    private final Set<String> existingGuids;
    private final List<EventItem> newEvents;
    private final String rssFilePath;

    public RaleighEventsRSSGenerator(String rssFilePath) {
        this.rssFilePath = requireNonNull(rssFilePath);
        this.existingGuids = new HashSet<>();
        this.newEvents = new ArrayList<>();
    }

    public static void main(String[] args) {
        // Bridge Java Util Logging to SLF4J for Selenium
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();

        if (args.length < 1) {
            logger.error("Usage: java visitraleigh.events.RaleighEventsRSSGenerator <rss-file-path>");
            System.exit(1);
        }

        String rssFilePath = args[0];
        RaleighEventsRSSGenerator generator = new RaleighEventsRSSGenerator(rssFilePath);

        try {
            generator.loadExistingFeed();
            generator.scrapeEvents();
            generator.generateRSSFeed();
            logger.info("RSS feed generated successfully with {} new events", generator.newEvents.size());
        } catch (Exception e) {
            logger.error("Error generating RSS feed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void loadExistingFeed() throws ParserConfigurationException, IOException, SAXException {
        File rssFile = new File(rssFilePath);
        if (!rssFile.exists()) {
            logger.info("No existing RSS feed found. Starting fresh.");
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

        logger.info("Loaded {} existing event GUIDs", existingGuids.size());
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() throws ParserConfigurationException {
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

    private void scrapeEvents() throws IOException {
        WebDriver driver = getWebDriver();

        try {
            int page = 1;
            int numPages = 1;
            while (page <= numPages) {
                String url = BASE_URL + "?page=" + page;
                logger.info("Scraping {}", url);

                long startTime = System.currentTimeMillis();
                driver.get(url);
                logger.debug("Waiting for page to load...");

                // Wait for a specific element to be present (most reliable)
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("li.arrow.arrow-next.arrow-double")));

                logger.debug("Page loaded in {}ms", (System.currentTimeMillis() - startTime));

                String pageSource = driver.getPageSource();

                if (DEBUG_MODE && page == 1) {
                    // Save first page for debugging
                    try (PrintWriter out = new PrintWriter(new FileWriter("debug-page.html"))) {
                        out.println(pageSource);
                    }
                    logger.debug("Debug: Page source saved to debug-page.html");
                    logger.debug("Debug: Page source length: {} characters", pageSource != null ? pageSource.length() : 0);
                }

                Document doc = Jsoup.parse(requireNonNull(pageSource), url);
                if (page == 1) {
                    numPages = getNumPages(doc);
                    logger.info("There are {} pages to parse.", numPages);
                }

                // Try to find all links that might be events (singular /event/)
                Elements allLinks = doc.select("a[href*='/event/']");
                logger.debug("Found {} links containing '/event/'", allLinks.size());

                // Filter to actual event links (not listing pages)
                Set<String> processedUrls = new HashSet<>();

                for (Element link : allLinks) {
                    String href = link.attr("abs:href");

                    // Skip if already processed
                    if (processedUrls.contains(href)) {
                        continue;
                    }

                    // Skip if not a visitraleigh event
                    if (!href.contains("visitraleigh.com/event/")) {
                        continue;
                    }

                    // Must be a specific event URL with slug and ID
                    if (!href.matches(".*/event/[^/]+/\\d+/?$")) {
                        continue;
                    }

                    processedUrls.add(href);

                    EventItem event = parseEventFromLink(link);

                    if (event != null) {
                        if (!existingGuids.add(event.guid)) {
                            logger.debug("Found existing event: {}", event.title);
                        } else {
                            newEvents.add(event);
                            logger.debug("Found new event: {}", event.title);
                        }
                    }
                }

                logger.info("Successfully parsed {} total events so far (page {} of {})",
                        newEvents.size(), page, numPages);

                page++;
            }
        } finally {
            driver.quit();
        }
    }

    private static WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        return new ChromeDriver(options);
    }

    private int getNumPages(Document doc) {
        final Elements doubleArrow = doc.select("li.arrow.arrow-next.arrow-double");
        if (doubleArrow.isEmpty()) {
            return DEFAULT_NUM_PAGES;
        }
        final Element first = doubleArrow.first();
        if (first == null) {
            return DEFAULT_NUM_PAGES;
        }
        final Elements children = first.children();
        if (children.isEmpty()) {
            return RaleighEventsRSSGenerator.DEFAULT_NUM_PAGES;
        }
        final Element firstChild = children.first();
        if (firstChild == null) {
            return RaleighEventsRSSGenerator.DEFAULT_NUM_PAGES;
        }
        final String href = firstChild.attr("href");
        if (href.isEmpty()) {
            return RaleighEventsRSSGenerator.DEFAULT_NUM_PAGES;
        }
        final String[] tokens = href.split("=");
        if (tokens.length < 2) {
            return RaleighEventsRSSGenerator.DEFAULT_NUM_PAGES;
        }
        try {
            return Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse number of pages", e);
            return RaleighEventsRSSGenerator.DEFAULT_NUM_PAGES;
        }
    }

    private EventItem parseEventFromLink(Element linkElement) {
        try {
            String eventUri = linkElement.attr("abs:href");

            if (DEBUG_MODE) {
                logger.trace("Parsing link: {}", eventUri);
            }

            final List<String> strings = Splitter.on("/").omitEmptyStrings().splitToList(eventUri);
            final String lastElement = strings.get(strings.size() - 1);
            int id = getEventId(lastElement);

            // Find the event card container - go up the tree to find it
            Element eventCard = linkElement;
            for (int i = 0; i < 10; i++) {
                Element parent = eventCard.parent();
                if (parent == null) break;

                String className = parent.className().toLowerCase();
                String tagName = parent.tagName().toLowerCase();

                // Look for common event card containers
                if (className.contains("event") || className.contains("card") ||
                        className.contains("result") || className.contains("listing") ||
                        tagName.equals("article") || className.contains("item")) {
                    eventCard = parent;
                    break;
                }
                eventCard = parent;
            }

            // Now look for title anywhere in the event card
            String title = "";

            // Try headings first
            Element heading = eventCard.selectFirst("h1, h2, h3, h4, h5, h6");
            if (heading != null) {
                title = heading.text().trim();
            }

            // Try title class
            if (title.length() < 3) {
                Element titleElem = eventCard.selectFirst("[class*='title'], [class*='Title'], [class*='name'], [class*='Name']");
                if (titleElem != null) {
                    title = titleElem.text().trim();
                }
            }

            // Try link with text (not the image link)
            if (title.length() < 3) {
                Elements links = eventCard.select("a[href*='/event/']");
                for (Element link : links) {
                    String linkText = link.text().trim();
                    if (linkText.length() > 3) {
                        title = linkText;
                        break;
                    }
                }
            }

            // Try image alt text
            if (title.length() < 3) {
                Element img = eventCard.selectFirst("img[alt]");
                if (img != null) {
                    String alt = img.attr("alt").trim();
                    if (alt.length() > 3) {
                        title = alt;
                    }
                }
            }

            // Try aria-label on the link
            if (title.length() < 3) {
                Elements linksWithAria = eventCard.select("a[aria-label]");
                for (Element link : linksWithAria) {
                    String ariaLabel = link.attr("aria-label").trim();
                    if (ariaLabel.length() > 3) {
                        title = ariaLabel;
                        break;
                    }
                }
            }

            if (title.length() < 3) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Could not extract title");
                    String eventCardHtml = eventCard.html();
                    logger.trace("Event card HTML (first 300 chars): {}",
                            eventCardHtml.substring(0, Math.min(300, eventCardHtml.length())));
                }
                return null;
            }

            if (DEBUG_MODE) {
                logger.trace("Title: {}", title);
            }

            // Look for date information in the event card
            String dateStr = "";
            Element dateElement = eventCard.selectFirst("time, [class*='date'], [class*='Date']");
            if (dateElement != null) {
                dateStr = dateElement.text().trim();
            }

            // Extract description from the event card
            String description = "";

            // Try to find block-meta div with dateInfo, times, location, region
            Element blockMeta = eventCard.selectFirst("div.block-meta, [class*='block-meta']");
            if (blockMeta != null) {
                StringBuilder descBuilder = new StringBuilder();
                UnaryOperator<String> liWrap = str -> "<br/>" + str;

                // Extract date info
                Element dateInfo = blockMeta.selectFirst("[class*='dateInfo'], [class*='date-info']");
                if (dateInfo != null) {
                    String dateText = dateInfo.text().trim();
                    if (!dateText.isEmpty()) {
                        descBuilder.append(liWrap.apply(dateText)).append(" ");
                    }
                }

                // Extract times
                Element times = blockMeta.selectFirst("[class*='times'], time");
                if (times != null) {
                    String timesText = times.text().trim();
                    if (!timesText.isEmpty()) {
                        descBuilder.append(liWrap.apply(timesText)).append(" ");
                    }
                }

                // Extract location
                Element location = blockMeta.selectFirst("[class*='location']");
                if (location != null) {
                    String locationText = location.text().trim();
                    if (!locationText.isEmpty()) {
                        descBuilder.append(liWrap.apply(locationText)).append(" ");
                    }
                }

                // Extract region
                Element region = blockMeta.selectFirst("[class*='region']");
                if (region != null) {
                    String regionText = region.text().trim();
                    if (!regionText.isEmpty()) {
                        descBuilder.append(liWrap.apply(regionText));
                    }
                }

                description = descBuilder.toString().trim();
            }

            // Fallback: try traditional description selectors
            if (description.isEmpty()) {
                Element descElement = eventCard.selectFirst("p, [class*='description'], [class*='excerpt']");
                if (descElement != null) {
                    description = descElement.text().trim();
                }
            }

            Element descElement = eventCard.selectFirst("p, [class*='description'], [class*='excerpt']");
            if (descElement != null) {
                description = descElement.text().trim();
            }

            // Extract image from the event card
            String imageUrl = "";
            Element imgElement = eventCard.selectFirst("img[src]");
            if (imgElement != null) {
                String src = imgElement.attr("abs:src");
                if (!src.contains("icon") && !src.contains("logo") && src.length() > 20) {
                    imageUrl = src;
                }
            }

            // Create full title with date
            String fullTitle = title;
            if (!dateStr.isEmpty()) {
                fullTitle = title + " (" + dateStr + ")";
            }

            return new EventItem(id, eventUri, fullTitle, description, eventUri, imageUrl, dateStr);

        } catch (Exception e) {
            if (DEBUG_MODE) {
                logger.trace("Error parsing link: {}", e.getMessage(), e);
            }
            return null;
        }
    }

    private static int getEventId(String lastElement) {
        int id = -1;
        try {
            id = Integer.parseInt(lastElement);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse value as int: {}", lastElement);
        }
        return id;
    }

    private void generateRSSFeed() throws ParserConfigurationException, TransformerException, IOException, SAXException {
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

        if (new File(rssFilePath).exists()) {
            org.w3c.dom.Document oldDoc = builder.parse(new File(rssFilePath));
            oldDoc.getDocumentElement().normalize();
            org.w3c.dom.NodeList oldItems = oldDoc.getElementsByTagName("item");

            for (int i = 0; i < oldItems.getLength(); i++) {
                org.w3c.dom.Node oldItem = oldItems.item(i);
                org.w3c.dom.Node importedNode = doc.importNode(oldItem, true);
                removeWhitespaceNodes(importedNode);
                channel.appendChild(importedNode);
            }
        }

        TransformerFactory transformerFactory = getTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(rssFilePath));
        transformer.transform(source, result);

        logger.info("RSS feed written to: {}", rssFilePath);
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

    private void addEventItem(org.w3c.dom.Document doc, org.w3c.dom.Element channel, EventItem event) {
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

    private record EventItem(int id, String guid, String title, String description, String link, String imageUrl,
                             String dateStr) implements Comparable<EventItem> {
        @Override
        public int compareTo(EventItem o) {
            return Integer.compare(this.id, o.id);
        }
    }
}
