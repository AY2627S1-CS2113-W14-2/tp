package command;

import tasks.Booking;
import tasks.BookingList;
import tasks.VenueCatalog;
import ui.Parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lists preset venues with no booking at the requested date and time.
 * @author Kytersaurus
 */
public class FreeCommand extends Command {
    private final Parser parser;

    /**
     * Creates the command with its input parser.
     * @author Kytersaurus
     * @param parser parser object
     */
    public FreeCommand(Parser parser) {
        assert parser != null : "Parser must not be null";
        this.parser = parser;
    }

    /**
     * Prints the available venues at specified time, line by line.
     * @author Kytersaurus
     * @param bookings Arraylist of Booking changed/read by command
     * @param item Additional variable used by command to select elements from bookings
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";

        LocalDateTime startTime = parser.parseFreeTime(item);
        List<String> venues = VenueCatalog.findAvailableVenues(bookings, startTime);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("Available venues from "
                + startTime.format(format) + " to "
                + startTime.plus(Booking.DEFAULT_DURATION).format(format) + ":");

        if (venues.isEmpty()) {
            System.out.println("No venues are available at specified time");
            return;
        }

        for (int i = 0; i < venues.size(); i++) {
            System.out.println((i+1) + ". " + venues.get(i));
        }
    }
}
