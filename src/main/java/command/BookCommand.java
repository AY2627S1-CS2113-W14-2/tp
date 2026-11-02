package command;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import ui.Parser;

import java.util.logging.Logger;

/**
 * Creates a booking from user input and adds it to the booking list.
 * @author Maxkedd
 */
public class BookCommand extends Command {
    /** Records diagnostic details about booking creation and validation. */
    private static final Logger LOGGER = Logger.getLogger(BookCommand.class.getName());

    private final Parser parser;

    /**
     * @param parser parser used to create a validated booking
     */
    public BookCommand(Parser parser) {
        assert parser != null : "Book command requires a parser";
        this.parser = parser;
    }

    /**
     * Rejects duplicate names (ignoring case) and venue/time clashes before adding a booking.
     * @author Maxkedd
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        Booking booking;
        try {
            booking = parser.parseBooking(item);
        } catch (InvalidCommandException e) {
            LOGGER.fine(() -> "Book rejected: " + e.getMessage());
            throw e;
        }
        if (bookings.findByName(booking.getName()) != -1) {
            LOGGER.fine(() -> "Book rejected: duplicate name=" + booking.getName());
            throw new InvalidCommandException("A booking with the name " + booking.getName()
                    + " already exists. Please use a different booking name.");
        }
        Booking clash = bookings.findClash(booking);
        if (clash != null) {
            LOGGER.fine(() -> "Book rejected: name=" + booking.getName()
                    + ", clashes with=" + clash.getName() + ", venue=" + clash.getVenue());
            throw new InvalidCommandException("Booking clash: " + clash.getVenue() + " is already booked at "
                    + clash.getTiming() + " for " + clash.getName() + ".");
        }
        bookings.add(booking);
        LOGGER.fine(() -> "Booking added: " + booking.getName() + ", stored count=" + bookings.size());
        System.out.println("Booking added: " + booking);
    }

}
