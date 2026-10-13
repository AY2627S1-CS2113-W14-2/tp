package Tasks;

/**
 * A named exam or lecture reservation with a venue and user-supplied timing.
 */
public class Booking extends Task {
    private final String name;
    private final String timing;
    private final String venue;

    /**
     * Creates a booking from fields already validated by the parser.
     */
    public Booking(String reason, String name, String venue, String timing) {
        super(reason);
        assert "exam".equals(reason) || "lecture".equals(reason) : "Booking reason must be exam or lecture";
        assert name != null && !name.isBlank() : "Booking name must already be validated";
        assert venue != null && !venue.isBlank() : "Booking venue must already be validated";
        assert timing != null && !timing.isBlank() : "Booking timing must already be validated";
        this.name = name;
        this.timing = timing;
        this.venue = venue;
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
     * Returns the type marker used for displaying a booking.
     */
    public String getTaskIcon() {
        return "B";
    }

    @Override
    public String toString() {
        return "[" + getTaskIcon() + "] " + getReason() + " - " + name
                + " (venue: " + venue + ", timing: " + timing + ")";
    }
}
