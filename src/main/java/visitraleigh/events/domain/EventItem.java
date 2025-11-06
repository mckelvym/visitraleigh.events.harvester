package visitraleigh.events.domain;

/**
 * Represents a single event with all its metadata.
 *
 * <p>This is an immutable record that holds all information about an event,
 * including its ID, URL, title, description, and image. Events are comparable
 * by their ID for sorting purposes.
 *
 * @param id The unique identifier for the event
 * @param guid The globally unique identifier (typically the event URL)
 * @param title The event title
 * @param description The event description
 * @param link The link to the event
 * @param imageUrl The URL of the event's image
 * @param dateStr The date string for the event
 */
public record EventItem(
        int id,
        String guid,
        String title,
        String description,
        String link,
        String imageUrl,
        String dateStr) implements Comparable<EventItem> {

    @Override
    public int compareTo(EventItem other) {
        return Integer.compare(this.id, other.id);
    }
}
