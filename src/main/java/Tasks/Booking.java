package Tasks;

/**
 * An exam or lecture reservation with a venue and user-supplied timing.
 */
public class Booking extends Task {
    private final String timing;
    private final String venue;

    /**
     * Creates a booking whose description is its booking reason.
     */
    public Booking(String reason, String venue, String timing) {
        super(reason);
        this.timing = timing;
        this.venue = venue;
    }

    public String getReason() {
        return description;
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
        return "[" + getTaskIcon() + "] " + getReason() + " (venue: " + venue + ", timing: " + timing + ")";
    }
}
