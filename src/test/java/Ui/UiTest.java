package Ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

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
            assertFalse(ui.processCommand("book r/exam v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/lecture v/LT2 t/2:00pm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(2, bookings.size());
        assertEquals("exam", bookings.get(0).getReason());
        assertEquals("lecture", bookings.get(1).getReason());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Booking added: [B] exam (venue: LT1, timing: 10:30am)"));
        assertTrue(response.contains("Booking added: [B] lecture (venue: LT2, timing: 2:00pm)"));
    }

    @Test
    void processCommand_invalidThenValid_recoversWithoutAddingInvalidBooking() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/meeting v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/exam v/LT1 t/"));
            assertTrue(bookings.isEmpty());
            assertFalse(ui.processCommand("book r/lecture v/LT2 t/2:00pm"));
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
}
