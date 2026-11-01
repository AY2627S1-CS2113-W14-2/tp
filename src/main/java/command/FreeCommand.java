package command;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import tasks.VenueCatalog;
import ui.Parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Lists available preset venues or a venue's available whole-hour slots.
 * @author Kytersaurus
 */
public class FreeCommand extends Command {
    /** Records diagnostic details about availability searches without changing console results. */
    private static final Logger LOGGER = Logger.getLogger(FreeCommand.class.getName());

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
     * Routes availability searches to venue results or daily slot results.
     * @author Kytersaurus
     * @param bookings Arraylist of Booking changed/read by command
     * @param item complete free command
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";

        Parser.FreeOptions options;
        try {
            options = parser.parseFreeOptions(item);
        } catch (InvalidCommandException e) {
            LOGGER.fine(() -> "Availability search rejected: " + e.getMessage());
            throw e;
        }
        if (options.venue() == null) {
            printAvailableVenues(bookings, options.startTime());
        } else {
            printAvailableSlots(bookings, options.venue(), options.date());
        }
    }

    /**
     * Prints preset venues available for the requested one-hour slot.
     * @author Kytersaurus
     * @param bookings stored bookings used to check venue availability
     * @param startTime start of the requested one-hour slot
     */
    private void printAvailableVenues(BookingList bookings, LocalDateTime startTime) {
        LOGGER.fine(() -> "Availability search: mode=venues, start=" + startTime
                + ", end=" + startTime.plus(Booking.DEFAULT_DURATION)
                + ", stored count=" + bookings.size());
        List<String> venues = VenueCatalog.findAvailableVenues(bookings, startTime);
        LOGGER.fine(() -> "Availability search completed: mode=venues, result count=" + venues.size());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("Available venues from "
                + startTime.format(format) + " to "
                + startTime.plus(Booking.DEFAULT_DURATION).format(format) + ":");

        if (venues.isEmpty()) {
            System.out.println("No venues are available at specified time");
            return;
        }

        for (int i = 0; i < venues.size(); i++) {
            System.out.println((i + 1) + ". " + venues.get(i));
        }
    }

    /**
     * Prints available one-hour slots starting on each whole hour of the requested date.
     * Each result includes its start and end date and time.
     * @author Kytersaurus
     * @param bookings stored bookings used to check venue availability
     * @param venue preset venue to search
     * @param date date whose available slots should be displayed
     */
    private void printAvailableSlots(BookingList bookings, String venue, LocalDate date) {
        LOGGER.fine(() -> "Availability search: mode=slots, venue=" + venue
                + ", date=" + date + ", stored count=" + bookings.size());
        List<LocalDateTime> slots = VenueCatalog.findAvailableSlots(bookings, venue, date);
        LOGGER.fine(() -> "Availability search completed: mode=slots, result count=" + slots.size());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter slotFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("Available one-hour slots for " + venue
                + " on " + date.format(dateFormat) + " (starts on the hour):");
        if (slots.isEmpty()) {
            System.out.println("No one-hour slots are available");
            return;
        }
        for (int i = 0; i < slots.size(); i++) {
            LocalDateTime start = slots.get(i);
            System.out.println((i + 1) + ". " + start.format(slotFormat) + " to "
                    + start.plus(Booking.DEFAULT_DURATION).format(slotFormat));
        }
    }
}
