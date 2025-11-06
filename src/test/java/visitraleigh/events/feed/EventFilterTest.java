package visitraleigh.events.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class EventFilterTest {

    private EventFilter filter;
    private static final DateTimeFormatter RFC_1123 = DateTimeFormatter.RFC_1123_DATE_TIME;

    @BeforeEach
    void setUp() {
        filter = new EventFilter(30);
    }

    @Test
    void shouldKeepEvent_withRecentDate_returnsTrue() {
        Node item = createMockItemWithDate(ZonedDateTime.now().minusDays(15));

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isTrue();
    }

    @Test
    void shouldKeepEvent_withOldDate_returnsFalse() {
        Node item = createMockItemWithDate(ZonedDateTime.now().minusDays(45));

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isFalse();
    }

    @Test
    void shouldKeepEvent_withTodayDate_returnsTrue() {
        Node item = createMockItemWithDate(ZonedDateTime.now());

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isTrue();
    }

    @Test
    void shouldKeepEvent_withExactlyCutoffDate_returnsFalse() {
        Node item = createMockItemWithDate(ZonedDateTime.now().minusDays(30));

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isFalse();
    }

    @Test
    void shouldKeepEvent_withNoPubDate_returnsTrue() {
        Node item = mock(Node.class);
        NodeList emptyChildren = mock(NodeList.class);
        when(emptyChildren.getLength()).thenReturn(0);
        when(item.getChildNodes()).thenReturn(emptyChildren);

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isTrue();
    }

    @Test
    void shouldKeepEvent_withInvalidPubDate_returnsTrue() {
        Node item = createMockItemWithInvalidDate();

        boolean shouldKeep = filter.shouldKeepEvent(item);

        assertThat(shouldKeep).isTrue();
    }

    @Test
    void extractPubDateFromItem_withValidDate_returnsDate() {
        ZonedDateTime expectedDate = ZonedDateTime.now().minusDays(10);
        Node item = createMockItemWithDate(expectedDate);

        ZonedDateTime extractedDate = filter.extractPubDateFromItem(item);

        assertThat(extractedDate).isNotNull();
        assertThat(extractedDate.getDayOfYear()).isEqualTo(expectedDate.getDayOfYear());
    }

    @Test
    void extractPubDateFromItem_withNoDate_returnsNull() {
        Node item = mock(Node.class);
        NodeList emptyChildren = mock(NodeList.class);
        when(emptyChildren.getLength()).thenReturn(0);
        when(item.getChildNodes()).thenReturn(emptyChildren);

        ZonedDateTime extractedDate = filter.extractPubDateFromItem(item);

        assertThat(extractedDate).isNull();
    }

    @Test
    void extractPubDateFromItem_withInvalidDate_returnsNull() {
        Node item = createMockItemWithInvalidDate();

        ZonedDateTime extractedDate = filter.extractPubDateFromItem(item);

        assertThat(extractedDate).isNull();
    }

    @Test
    void findLastPubDate_withItems_returnsLastDate() {
        ZonedDateTime expectedDate = ZonedDateTime.now().minusDays(20);
        Node lastItem = createMockItemWithDate(expectedDate);

        NodeList items = mock(NodeList.class);
        when(items.getLength()).thenReturn(5);
        when(items.item(4)).thenReturn(lastItem);

        Optional<ZonedDateTime> lastDate = filter.findLastPubDate(items);

        assertThat(lastDate).isPresent();
        assertThat(lastDate.get().getDayOfYear()).isEqualTo(expectedDate.getDayOfYear());
    }

    @Test
    void findLastPubDate_withNoItems_returnsEmpty() {
        NodeList items = mock(NodeList.class);
        when(items.getLength()).thenReturn(0);

        Optional<ZonedDateTime> lastDate = filter.findLastPubDate(items);

        assertThat(lastDate).isEmpty();
    }

    @Test
    void findLastPubDate_withLastItemHavingNoDate_returnsEmpty() {
        Node lastItem = mock(Node.class);
        NodeList emptyChildren = mock(NodeList.class);
        when(emptyChildren.getLength()).thenReturn(0);
        when(lastItem.getChildNodes()).thenReturn(emptyChildren);

        NodeList items = mock(NodeList.class);
        when(items.getLength()).thenReturn(1);
        when(items.item(0)).thenReturn(lastItem);

        Optional<ZonedDateTime> lastDate = filter.findLastPubDate(items);

        assertThat(lastDate).isEmpty();
    }

    @Test
    void constructor_with7Days_usesShorterCutoff() {
        EventFilter shortFilter = new EventFilter(7);
        Node recentItem = createMockItemWithDate(ZonedDateTime.now().minusDays(10));

        boolean shouldKeep = shortFilter.shouldKeepEvent(recentItem);

        assertThat(shouldKeep).isFalse();
    }

    @Test
    void constructor_with90Days_usesLongerCutoff() {
        EventFilter longFilter = new EventFilter(90);
        Node oldItem = createMockItemWithDate(ZonedDateTime.now().minusDays(60));

        boolean shouldKeep = longFilter.shouldKeepEvent(oldItem);

        assertThat(shouldKeep).isTrue();
    }

    private Node createMockItemWithDate(ZonedDateTime date) {
        String dateStr = date.format(RFC_1123);

        Node pubDateNode = mock(Node.class);
        when(pubDateNode.getNodeName()).thenReturn("pubDate");
        when(pubDateNode.getTextContent()).thenReturn(dateStr);

        NodeList children = mock(NodeList.class);
        when(children.getLength()).thenReturn(1);
        when(children.item(0)).thenReturn(pubDateNode);

        Node item = mock(Node.class);
        when(item.getChildNodes()).thenReturn(children);

        return item;
    }

    private Node createMockItemWithInvalidDate() {
        Node pubDateNode = mock(Node.class);
        when(pubDateNode.getNodeName()).thenReturn("pubDate");
        when(pubDateNode.getTextContent()).thenReturn("invalid date string");

        NodeList children = mock(NodeList.class);
        when(children.getLength()).thenReturn(1);
        when(children.item(0)).thenReturn(pubDateNode);

        Node item = mock(Node.class);
        when(item.getChildNodes()).thenReturn(children);

        return item;
    }
}
