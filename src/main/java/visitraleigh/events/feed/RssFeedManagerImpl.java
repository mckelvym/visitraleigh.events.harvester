package visitraleigh.events.feed;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import visitraleigh.events.domain.EventItem;

/**
 * RSS feed management implementation with XXE protection.
 *
 * <p>This class handles the complete RSS feed lifecycle:
 * <ul>
 *   <li>Loading existing feed and extracting GUIDs</li>
 *   <li>Generating new RSS 2.0 XML documents</li>
 *   <li>Merging new and existing events</li>
 *   <li>Filtering events by age</li>
 *   <li>Writing feed to file with proper formatting</li>
 * </ul>
 *
 * <p>All XML processing uses secure configurations to prevent XXE attacks.
 */
public class RssFeedManagerImpl implements RssFeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(RssFeedManagerImpl.class);

    private final XmlSecurityConfigurer securityConfigurer;
    private final EventFilter eventFilter;

    /**
     * Creates a new RSS feed manager.
     *
     * @param dropEventsOlderThanDays The age threshold for dropping old events
     */
    public RssFeedManagerImpl(int dropEventsOlderThanDays) {
        this.securityConfigurer = new XmlSecurityConfigurer();
        this.eventFilter = new EventFilter(dropEventsOlderThanDays);
    }

    @Override
    public Set<String> loadExistingGuids(String filePath) throws Exception {
        File rssFile = new File(filePath);
        if (!rssFile.exists()) {
            LOG.info("No existing RSS feed found at: {}", filePath);
            return new HashSet<>();
        }

        DocumentBuilderFactory factory = securityConfigurer.createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(rssFile);

        NodeList guidNodes = doc.getElementsByTagName("guid");
        Set<String> guids = new HashSet<>();

        for (int i = 0; i < guidNodes.getLength(); i++) {
            String guid = guidNodes.item(i).getTextContent();
            guids.add(guid);
        }

        LOG.info("Loaded {} existing event GUIDs from feed", guids.size());
        return guids;
    }

    @Override
    public void generateFeed(
            String filePath,
            List<EventItem> newEvents,
            String channelTitle,
            String channelLink,
            String channelDescription) throws Exception {

        LOG.info("Generating RSS feed with {} new events", newEvents.size());

        // Create secure document builder
        DocumentBuilderFactory factory = securityConfigurer.createSecureDocumentBuilderFactory();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Build RSS structure
        Element rss = doc.createElement("rss");
        rss.setAttribute("version", "2.0");
        doc.appendChild(rss);

        Element channel = doc.createElement("channel");
        rss.appendChild(channel);

        // Add channel metadata
        addElement(doc, channel, "title", channelTitle);
        addElement(doc, channel, "link", channelLink);
        addElement(doc, channel, "description", channelDescription);
        addElement(doc, channel, "language", "en-us");
        addElement(doc, channel, "lastBuildDate",
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        // Add new events (sorted by ID, descending)
        newEvents.sort(Comparator.reverseOrder());
        for (EventItem event : newEvents) {
            addEventItem(doc, channel, event);
        }

        // Import existing events (filtered by age)
        int droppedEventsCount = importExistingEvents(filePath, builder, doc, channel);

        // Log statistics
        NodeList items = doc.getElementsByTagName("item");
        Optional<ZonedDateTime> oldestDate = eventFilter.findLastPubDate(items);
        eventFilter.logFeedStatistics(items.getLength(), droppedEventsCount,
                oldestDate.orElse(null));

        // Write RSS to file
        writeRssToFile(doc, filePath);
    }

    /**
     * Imports existing events from an old RSS file, filtering by age.
     *
     * @param filePath The path to the old RSS file
     * @param builder The document builder
     * @param doc The new document to import into
     * @param channel The channel element to append items to
     * @return The number of events dropped due to age
     * @throws Exception if import fails
     */
    private int importExistingEvents(
            String filePath,
            DocumentBuilder builder,
            Document doc,
            Element channel) throws Exception {

        File oldFile = new File(filePath);

        if (!oldFile.exists()) {
            return 0;
        }

        Document oldDoc = builder.parse(oldFile);
        oldDoc.getDocumentElement().normalize();
        NodeList oldItems = oldDoc.getElementsByTagName("item");

        LOG.info("Importing {} existing events from feed", oldItems.getLength());

        int droppedEventsCount = 0;
        for (int i = 0; i < oldItems.getLength(); i++) {
            Node oldItem = oldItems.item(i);

            // Filter by age
            if (!eventFilter.shouldKeepEvent(oldItem)) {
                droppedEventsCount++;
                continue;
            }

            // Import and clean up the node
            Node importedNode = doc.importNode(oldItem, true);
            removeWhitespaceNodes(importedNode);
            channel.appendChild(importedNode);
        }

        return droppedEventsCount;
    }

    /**
     * Writes the RSS document to a file with proper formatting.
     *
     * @param doc The RSS document
     * @param filePath The output file path
     * @throws Exception if writing fails
     */
    private void writeRssToFile(Document doc, String filePath) throws Exception {
        TransformerFactory transformerFactory = securityConfigurer.createSecureTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);

        LOG.info("RSS feed written to: {}", filePath);
    }

    /**
     * Removes whitespace-only text nodes from a DOM tree.
     *
     * <p>This is necessary to ensure proper indentation when writing XML.
     *
     * @param node The root node to clean
     */
    private void removeWhitespaceNodes(Node node) {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    /**
     * Adds an event item to the RSS document.
     *
     * @param doc The XML document
     * @param channel The channel element
     * @param event The event to add
     */
    private void addEventItem(Document doc, Element channel, EventItem event) {
        Element item = doc.createElement("item");
        channel.appendChild(item);

        addElement(doc, item, "title", event.title());
        addElement(doc, item, "link", event.link());
        addElement(doc, item, "description", event.description());
        addElement(doc, item, "guid", event.guid());
        addElement(doc, item, "pubDate",
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        if (!event.imageUrl().isEmpty()) {
            Element enclosure = doc.createElement("enclosure");
            enclosure.setAttribute("url", event.imageUrl());
            enclosure.setAttribute("type", "image/jpeg");
            item.appendChild(enclosure);
        }
    }

    /**
     * Adds a simple text element to a parent element.
     *
     * @param doc The XML document
     * @param parent The parent element
     * @param tagName The tag name for the new element
     * @param textContent The text content
     */
    private void addElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }
}
