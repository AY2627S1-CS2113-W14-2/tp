package command;

import Tasks.Booking;
import Tasks.BookingList;
import ui.Parser;

/**
 * Creates a booking from user input and adds it to the booking list.
 */
public class BookCommand extends Command {
    private final Parser parser;

    /**
     * Reuses the UI's parser to validate booking input before changing the list.
     * @param parser parser used to create a validated booking
     */
    public BookCommand(Parser parser) {
        assert parser != null : "Book command requires a parser";
        this.parser = parser;
    }

    /**
     * Adds a valid booking and prints its confirmation.
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        Booking booking = parser.parseBooking(item);
        bookings.add(booking);
        System.out.println("Booking added: " + booking);
    }
}
