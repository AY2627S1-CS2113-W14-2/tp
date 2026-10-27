package tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * A named exam or lecture reservation with a venue and user-supplied timing.
 * @author Maxkedd
 */
public class Booking extends Task {
    private static final DateTimeFormatter TIMING_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final String name;
    private final String timing;
    private final String venue;
    /**
     * Optional expected attendee count; null means the booking size is unknown.
     */
    private final Integer attendeeCount;
    // Null indicates an older saved booking whose timing has no full date.
    private final LocalDateTime startTime;

    /**
     * Creates a booking from fields already validated by the parser.
     * @author Maxkedd
     */
    public Booking(String reason, String name, String venue, String timing) {
        this(reason, name, venue, timing, null);
    }

    /**
     * Creates a booking with an optional expected attendee count.
     */
    public Booking(String reason, String name, String venue, String timing, Integer attendeeCount) {
        super(reason);
        assert "exam".equals(reason) || "lecture".equals(reason) : "Booking reason must be exam or lecture";
        assert name != null && !name.isBlank() : "Booking name must already be validated";
        assert venue != null && !venue.isBlank() : "Booking venue must already be validated";
        assert timing != null && !timing.isBlank() : "Booking timing must already be validated";
        assert attendeeCount == null || attendeeCount > 0 : "Attendee count must be positive when provided";
        this.name = name;
        this.timing = timing;
        this.venue = venue;
        this.attendeeCount = attendeeCount;
        this.startTime = parseTimingIfValid(timing);
    }

    /**
     * Parses a date and time in dd/MM/yyyy HH:mm format.
     *
     * @param timing date and time supplied by the user
     * @return parsed date and time
     * @throws DateTimeParseException if the timing is not in the required format
     */
    public static LocalDateTime parseStartTime(String timing) {
        return LocalDateTime.parse(timing, TIMING_FORMAT);
    }

    /**
     * Allows older saved bookings to remain viewable if their timing is not sortable.
     */
    private static LocalDateTime parseTimingIfValid(String timing) {
        try {
            return parseStartTime(timing);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public String getReason() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return this.venue;
    }

    public String getTiming() {
        return this.timing;
    }

    /**
     * Returns the expected attendee count, or null when the booking size is unknown.
     */
    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    /**
     * Returns the parsed start time, or null when this is an older booking without a full date.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the type marker used for displaying a booking.
     */
    public String getTaskIcon() {
        return "B";
    }

    @Override
    public String toString() {
        String details = "[" + getTaskIcon() + "] " + getReason() + " - " + name
                + " (venue: " + venue + ", timing: " + timing;
        if (attendeeCount != null) {
            details += ", attendees: " + attendeeCount;
        }
        return details + ")";
    }
}
