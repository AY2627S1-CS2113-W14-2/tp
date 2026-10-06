package Ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Checks that commands add bookings only after validation and give useful feedback.
 */
class UiTest {
    @Test
    void processCommand_validBookings_addsBothAndConfirms() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/exam n/CS2113 Midterm v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/2:00pm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(2, bookings.size());
        assertEquals("exam", bookings.get(0).getReason());
        assertEquals("CS2113 Midterm", bookings.get(0).getName());
        assertEquals("lecture", bookings.get(1).getReason());
        assertEquals("Week 1 Lecture", bookings.get(1).getName());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Booking added: [B] exam - CS2113 Midterm (venue: LT1, timing: 10:30am)"));
        assertTrue(response.contains("Booking added: [B] lecture - Week 1 Lecture (venue: LT2, timing: 2:00pm)"));
    }

    @Test
    void processCommand_invalidThenValid_recoversWithoutAddingInvalidBooking() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/meeting n/Meeting v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/exam n/Midterm v/LT1 t/"));
            assertFalse(ui.processCommand("book r/exam n/ v/LT1 t/10:30am"));
            assertTrue(bookings.isEmpty());
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/2:00pm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(1, bookings.size());
        assertEquals("lecture", bookings.get(0).getReason());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Booking reason must be exam or lecture."));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
    }

    @Test
    void processCommand_bye_exitsWithoutAddingBooking() {
        BookingList bookings = new BookingList();

        assertTrue(new Ui(bookings).processCommand("BYE"));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void processCommand_cancelExistingBooking_removesIt() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("cancel n/cs2113 midterm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(bookings.isEmpty());
        assertTrue(output.toString(StandardCharsets.UTF_8)
                .contains("Booking cancelled: [B] exam - CS2113 Midterm (venue: LT1, timing: 10:30am)"));
    }

    @Test
    void processCommand_cancelUnknownBooking_keepsListUnchanged() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("cancel n/Nonexistent"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(1, bookings.size());
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No booking found with the name: Nonexistent"));
    }
}
