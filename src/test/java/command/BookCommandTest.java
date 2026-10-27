package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import ui.Parser;

/** Checks that direct booking execution validates before changing the list. */
class BookCommandTest {
    private static final String VALID_COMMAND = "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00";

    @Test
    void execute_validInput_addsExactlyOneBooking() {
        BookingList bookings = new BookingList();

        new BookCommand(new Parser()).execute(bookings, VALID_COMMAND);

        assertEquals(1, bookings.size());
        assertEquals("Midterm", bookings.getLast().getName());
    }

    @Test
    void execute_clashingInput_preservesOriginalBooking() {
        BookingList bookings = new BookingList();
        Booking original = new Booking("lecture", "Original", "lt1", "05/10/2026 14:00");
        bookings.add(original);

        InvalidCommandException error = assertThrows(InvalidCommandException.class,
                () -> new BookCommand(new Parser()).execute(bookings, VALID_COMMAND));

        assertEquals("Booking clash: lt1 is already booked at 05/10/2026 14:00 for Original.", error.getMessage());
        assertEquals(1, bookings.size());
        assertSame(original, bookings.getLast());
    }

    /**
     * Rejects existing names despite differences in case, surrounding spaces, venue, time or reason.
     * @author Maxkedd
     */
    @Test
    void execute_duplicateName_preservesOriginalBooking() {
        BookingList bookings = new BookingList();
        Booking original = new Booking("lecture", "Midterm", "LT2", "06/10/2026 09:00");
        bookings.add(original);
        BookCommand command = new BookCommand(new Parser());

        for (String name : new String[]{"Midterm", "midterm", "MIDTERM", "  Midterm  "}) {
            InvalidCommandException error = assertThrows(InvalidCommandException.class,
                    () -> command.execute(bookings, "book r/exam n/" + name + " v/LT1 t/05/10/2026 14:00"));

            assertEquals("A booking with the name " + name.strip()
                    + " already exists. Please use a different booking name.", error.getMessage());
            assertEquals(1, bookings.size());
            assertSame(original, bookings.get(0));
        }
    }

    /**
     * Allows a distinct name even when it contains an existing name as a substring.
     * @author Maxkedd
     */
    @Test
    void execute_similarButDistinctName_addsBooking() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("lecture", "Midterm Review", "LT2", "06/10/2026 09:00"));

        new BookCommand(new Parser()).execute(bookings, VALID_COMMAND);

        assertEquals(2, bookings.size());
        assertEquals("Midterm", bookings.getLast().getName());
    }

    @Test
    void execute_malformedInput_doesNotAddBooking() {
        BookingList bookings = new BookingList();
        assertThrows(InvalidCommandException.class,
                () -> new BookCommand(new Parser()).execute(bookings, "book r/exam n/Midterm"));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void constructorAndExecute_nullDependencies_failPreconditions() {
        assertThrows(AssertionError.class, () -> new BookCommand(null));
        assertThrows(AssertionError.class, () -> new BookCommand(new Parser()).execute(null, VALID_COMMAND));
    }
}
