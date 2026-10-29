package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;

/** Checks filtered selection, safe replacement, and recovery during interactive editing. */
class EditCommandTest {
    private final BookingList bookings = new BookingList();
    private final EditCommand command = new EditCommand();

    @Test
    void execute_duplicateNames_numbersOnlyExactMatchesAndEditsChosenBooking() {
        Booking unrelated = addBooking("Other", "LT0");
        Booking first = addBooking("Midterm", "LT1");
        Booking partial = addBooking("Midterm Review", "LT2");
        Booking second = addBooking("MIDTERM", "LT3");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            command.execute(bookings, "midterm");
        } finally {
            System.setOut(originalOutput);
        }
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("1. " + first), response);
        assertTrue(response.contains("2. " + second), response);
        assertFalse(response.contains("Midterm Review"), response);
        assertFalse(response.contains("Other"), response);
        assertTrue(command.isPending());
        command.processInput(bookings, "2");
        assertSame(second, bookings.get(3));

        command.processInput(bookings, "v/LT4");

        assertEquals(4, bookings.size());
        assertSame(unrelated, bookings.get(0));
        assertSame(first, bookings.get(1));
        assertSame(partial, bookings.get(2));
        assertEquals("LT4", bookings.get(3).getVenue());
        assertEquals("MIDTERM", bookings.get(3).getName());
        assertEquals(second.getTiming(), bookings.get(3).getTiming());
        assertFalse(command.isPending());
    }

    @Test
    void processInput_singleMatch_updatesAllDetailsWithoutSelection() {
        addBooking("Midterm", "LT1");
        command.execute(bookings, "MIDTERM");

        command.processInput(bookings, "t/06/10/2026 15:00 v/Lecture Theatre 2 n/Final Exam r/LECTURE");

        Booking edited = bookings.get(0);
        assertEquals("Final Exam", edited.getName());
        assertEquals("lecture", edited.getReason());
        assertEquals("Lecture Theatre 2", edited.getVenue());
        assertEquals("06/10/2026 15:00", edited.getTiming());
        assertFalse(command.isPending());
    }

    @Test
    void processInput_nameOnlyEdit_doesNotClashWithItself() {
        Booking original = addBooking("Midterm", "LT1");
        command.execute(bookings, "Midterm");

        command.processInput(bookings, "n/New Name");

        assertEquals("New Name", bookings.get(0).getName());
        assertEquals(original.getVenue(), bookings.get(0).getVenue());
        assertEquals(original.getTiming(), bookings.get(0).getTiming());
    }

    @Test
    void processInput_invalidSelections_preservesBookingsAndAllowsRetry() {
        Booking first = addBooking("Midterm", "LT1");
        Booking second = addBooking("Midterm", "LT2");
        command.execute(bookings, "Midterm");

        for (String selection : new String[]{"0", "-1", "3", "abc", "1.5", "999999999999999"}) {
            assertThrows(InvalidCommandException.class, () -> command.processInput(bookings, selection), selection);
            assertSame(first, bookings.get(0));
            assertSame(second, bookings.get(1));
            assertTrue(command.isPending());
        }
        command.processInput(bookings, " 1 ");
        command.processInput(bookings, "n/Updated");
        assertEquals("Updated", bookings.get(0).getName());
        assertSame(second, bookings.get(1));
    }

    @Test
    void processInput_invalidDetailsAndClash_preservesOriginalAndAllowsRetry() {
        Booking original = addBooking("Midterm", "LT1");
        Booking occupied = addBooking("Other", "LT2");
        command.execute(bookings, "Midterm");

        for (String fields : new String[]{"r/meeting", "n/", "t/31/02/2026 14:00", "v/lt2"}) {
            assertThrows(InvalidCommandException.class, () -> command.processInput(bookings, fields), fields);
            assertSame(original, bookings.get(0));
            assertSame(occupied, bookings.get(1));
            assertTrue(command.isPending());
        }
        command.processInput(bookings, "v/LT3");
        assertEquals("LT3", bookings.get(0).getVenue());
        assertFalse(command.isPending());
    }

    @Test
    void processInput_overlappingHour_preservesOriginalAndAllowsRetry() {
        Booking original = addBooking("Midterm", "LT1");
        Booking occupied = addBooking("Other", "LT2");
        command.execute(bookings, "Midterm");

        for (String time : new String[]{"13:01", "13:59", "14:00", "14:01", "14:59"}) {
            String fields = "v/lt2 t/05/10/2026 " + time;
            InvalidCommandException error = assertThrows(InvalidCommandException.class,
                    () -> command.processInput(bookings, fields), fields);

            assertTrue(error.getMessage().contains("Booking clash:"));
            assertEquals(2, bookings.size());
            assertSame(original, bookings.get(0));
            assertSame(occupied, bookings.get(1));
            assertTrue(command.isPending());
        }

        command.processInput(bookings, "v/lt2 t/05/10/2026 15:00");
        assertEquals("05/10/2026 15:00", bookings.get(0).getTiming());
        assertFalse(command.isPending());
    }

    @Test
    void processInput_adjacentHourBeforeOrAfterExistingBooking_succeeds() {
        addBooking("Midterm", "LT1");
        Booking occupied = addBooking("Other", "LT2");

        for (String time : new String[]{"13:00", "15:00"}) {
            command.execute(bookings, "Midterm");
            command.processInput(bookings, "v/lt2 t/05/10/2026 " + time);

            assertEquals(2, bookings.size());
            assertEquals("05/10/2026 " + time, bookings.get(0).getTiming());
            assertSame(occupied, bookings.get(1));
            assertFalse(command.isPending());
        }
    }

    @Test
    void processInput_overlapAcrossMidnight_rejectsUntilExistingBookingEnds() {
        Booking original = addBooking("Midterm", "LT1");
        Booking occupied = new Booking("exam", "Other", "LT2", "05/10/2026 23:30");
        bookings.add(occupied);
        command.execute(bookings, "Midterm");

        assertThrows(InvalidCommandException.class,
                () -> command.processInput(bookings, "v/LT2 t/06/10/2026 00:15"));
        assertSame(original, bookings.get(0));
        assertSame(occupied, bookings.get(1));
        assertTrue(command.isPending());

        command.processInput(bookings, "v/LT2 t/06/10/2026 00:30");
        assertEquals("06/10/2026 00:30", bookings.get(0).getTiming());
        assertFalse(command.isPending());
    }

    @Test
    void processInput_legacyBookingWithoutFullDate_doesNotPreventEdit() {
        addBooking("Midterm", "LT1");
        Booking legacy = new Booking("exam", "Legacy", "LT2", "14:00");
        bookings.add(legacy);
        command.execute(bookings, "Midterm");

        command.processInput(bookings, "v/LT2");

        assertEquals("LT2", bookings.get(0).getVenue());
        assertSame(legacy, bookings.get(1));
        assertFalse(command.isPending());
    }

    @Test
    void processInput_abortDuringSelectionOrEditing_preservesBookingsAndClearsPendingEdit() {
        Booking first = addBooking("Midterm", "LT1");
        Booking second = addBooking("Midterm", "LT2");
        command.execute(bookings, "Midterm");
        command.processInput(bookings, "abort");
        assertFalse(command.isPending());

        command.execute(bookings, "Midterm");
        command.processInput(bookings, "2");
        command.processInput(bookings, " ABORT ");

        assertFalse(command.isPending());
        assertSame(first, bookings.get(0));
        assertSame(second, bookings.get(1));
    }

    @Test
    void execute_missingName_reportsErrorWithoutStartingEdit() {
        Booking original = addBooking("Midterm Review", "LT1");

        assertThrows(InvalidCommandException.class, () -> command.execute(bookings, "Midterm"));

        assertFalse(command.isPending());
        assertSame(original, bookings.get(0));
    }

    /** Adds a booking at the shared test time, with a distinct venue when needed. */
    private Booking addBooking(String name, String venue) {
        Booking booking = new Booking("exam", name, venue, "05/10/2026 14:00");
        bookings.add(booking);
        return booking;
    }
}
