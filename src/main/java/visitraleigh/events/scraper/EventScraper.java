package visitraleigh.events.scraper;

import java.util.List;
import java.util.Set;
import visitraleigh.events.domain.EventItem;

/**
 * Interface for scraping events from a website.
 *
 * <p>This interface defines the contract for event scraping operations.
 * Implementations are responsible for navigating web pages, discovering
 * event links, and parsing event details.
 */
public interface EventScraper {

    /**
     * Scrapes events from the website.
     *
     * @param existingGuids set of GUIDs that already exist in the feed
     * @return list of new EventItem objects
     */
    List<EventItem> scrapeEvents(Set<String> existingGuids);
}
