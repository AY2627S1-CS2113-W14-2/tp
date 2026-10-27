package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import ui.Parser;

/**
 * Checks history ordering and which successful operations are eligible for undo.
 * @author Maxkedd
 */
class UndoCommandTest {
    /**
     * Checks that unsuccessful and read-only commands do not consume an undo step.
     * @author Maxkedd
     */
    @Test
    void executeAndRecord_readOnlyAndFailedCommands_doNotReplaceLastSuccessfulCommand() {
        BookingList bookings = new BookingList();
        UndoCommand undo = new UndoCommand();
        BookCommand book = new BookCommand(new Parser());
        String bookingInput = "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00";
        String clashingInput = "book r/exam n/Clash v/LT1 t/05/10/2026 14:00";
        undo.executeAndRecord("book", bookingInput, bookings, () -> book.execute(bookings, bookingInput));
        undo.executeAndRecord("list", "list", bookings, () -> new ListCommand().execute(bookings, "bookings"));
        assertThrows(InvalidCommandException.class,
                () -> undo.executeAndRecord("book", clashingInput, bookings,
                        () -> book.execute(bookings, clashingInput)));

        undo.execute(bookings, "/undo");

        assertTrue(bookings.isEmpty());
    }

    /**
     * Checks that undo restores duplicate bookings and their original order one step at a time.
     * @author Maxkedd
     */
    @Test
    void execute_repeatedUndo_restoresStatesInReverseOrder() {
        BookingList bookings = new BookingList();
        Booking first = new Booking("exam", "Same name", "LT1", "05/10/2026 14:00");
        Booking second = new Booking("exam", "Same name", "LT2", "05/10/2026 14:00");
        UndoCommand undo = new UndoCommand();
        undo.executeAndRecord("book", "book r/exam n/Same name v/LT1 t/05/10/2026 14:00",
                bookings, () -> bookings.add(first));
        undo.executeAndRecord("book", "book r/exam n/Same name v/LT2 t/05/10/2026 14:00",
                bookings, () -> bookings.add(second));
        undo.executeAndRecord("cancel", "cancel n/Same name", bookings, () -> bookings.remove(0));

        undo.execute(bookings, "/undo");
        assertEquals(2, bookings.size());
        assertEquals(first, bookings.get(0));
        assertEquals(second, bookings.get(1));

        undo.execute(bookings, "/undo");
        assertEquals(1, bookings.size());
        assertEquals(first, bookings.get(0));

        undo.execute(bookings, "/undo");
        assertTrue(bookings.isEmpty());
        undo.execute(bookings, "/undo");
        assertTrue(bookings.isEmpty());
    }

    /**
     * Checks the declared set of undoable commands.
     * @author Maxkedd
     */
    @Test
    void isUndoable_onlyCommandsThatChangeBookings_areEligible() {
        UndoCommand undo = new UndoCommand();
        assertTrue(undo.isUndoable("book"));
        assertTrue(undo.isUndoable("cancel"));
        for (String command : new String[]{
            "list", "find", "viewbooking", "calendar", "bye", "/undo", "/redo", "unknown"}) {
            assertFalse(undo.isUndoable(command), command);
        }
    }
}
