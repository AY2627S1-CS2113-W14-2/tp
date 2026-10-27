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
