package visitraleigh.events.domain;

import java.time.LocalDate;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Immutable record representing an event item.
 * Contains all metadata needed for RSS feed generation.
 */
public record EventItem(
    String id,
    String title,
    String link,
    @Nullable String description,
    LocalDate eventDateStart,
    @Nullable LocalDate eventDateEnd,
    @Nullable String imageUrl,
    @Nullable String location
) {
    /**
     * Creates an EventItem with validation.
     */
    public EventItem {
        requireNonNull(id, "id cannot be null");
        requireNonNull(title, "title cannot be null");
        requireNonNull(link, "link cannot be null");
        requireNonNull(eventDateStart, "eventDateStart cannot be null");
    }

    /**
     * Returns the GUID for RSS feed generation.
     */
    public String guid() {
        return link;
    }

    /**
     * Returns whether this event has an associated image.
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    /**
     * Returns a sanitized description suitable for RSS feed.
     */
    public String sanitizedDescription() {
        if (description == null || description.isBlank()) {
            return "";
        }
        return description.trim();
    }
}
