package command;

import java.util.Map;
import java.util.TreeMap;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;

/**
 * Reports the largest recorded attendee count for each venue.
 * @author produdepunk
 */
public class SizeCommand extends Command {

    /**
     * Prints each venue's maximum attendee count among bookings that include a count.
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        assert item != null : "Check size input cannot be null";
        if (!item.strip().equalsIgnoreCase("checksize")) {
            throw new InvalidCommandException("Please use: checksize");
        }

        Map<String, Integer> maximumByVenue = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            Integer attendeeCount = booking.getAttendeeCount();
            if (attendeeCount == null) {
                continue;
            }
            maximumByVenue.merge(booking.getVenue(), attendeeCount, Math::max);
        }

        if (maximumByVenue.isEmpty()) {
            System.out.println("No bookings include an attendee count.");
            return;
        }
        System.out.println("Largest recorded attendee count by venue:");
        for (Map.Entry<String, Integer> entry : maximumByVenue.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " attendees");
        }
    }
}
