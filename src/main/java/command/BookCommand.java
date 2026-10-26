package command;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;
import ui.Parser;

/**
 * Creates a booking from user input and adds it to the booking list.
 * @author Maxkedd
 */
public class BookCommand extends Command {
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
        Booking booking = parser.parseBooking(item);
        if (bookings.findByName(booking.getName()) != -1) {
            throw new InvalidCommandException("A booking with the name " + booking.getName()
                    + " already exists. Please use a different booking name.");
        }
        Booking clash = bookings.findClash(booking);
        if (clash != null) {
            throw new InvalidCommandException("Booking clash: " + clash.getVenue() + " is already booked at "
                    + clash.getTiming() + " for " + clash.getName() + ".");
        }
        bookings.add(booking);
        System.out.println("Booking added: " + booking);
    }

}
