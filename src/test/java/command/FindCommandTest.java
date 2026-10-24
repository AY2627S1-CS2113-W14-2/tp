package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Tests that the find command shows the right bookings and never changes the list.
 */
class FindCommandTest {
    private BookingList createBookings() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "05/10/2026 14:00"));
        bookings.add(new Booking("exam", "CS2445 Final Exam", "LT2", "06/10/2026 09:00"));
        bookings.add(new Booking("lecture", "MA1521 Lecture", "LT3", "07/10/2026 10:00"));
        return bookings;
    }

    private String runFind(BookingList bookings, String keyword) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            new FindCommand().execute(bookings, keyword);
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void execute_partialKeywordInAnyCase_printsOnlyMatchesInOrder() {
        BookingList bookings = createBookings();

        String response = runFind(bookings, "cs2");

        assertTrue(response.contains("1. [B] exam - CS2113 Midterm"), response);
        assertTrue(response.contains("2. [B] exam - CS2445 Final Exam"), response);
        assertFalse(response.contains("MA1521"), response);
        assertEquals(3, bookings.size());
    }

    @Test
    void execute_keywordInsideName_matchesMiddleOfName() {
        String response = runFind(createBookings(), "final");

        assertTrue(response.contains("1. [B] exam - CS2445 Final Exam"), response);
        assertFalse(response.contains("CS2113"), response);
    }

    @Test
    void execute_noMatchOrInvalidInput_isHandledSafely() {
        BookingList bookings = createBookings();

        assertTrue(runFind(bookings, "Tutorial").contains("No bookings found matching: Tutorial"));
        assertEquals(3, bookings.size());
        assertThrows(AssertionError.class, () -> new FindCommand().execute(bookings, " "));
        assertThrows(AssertionError.class, () -> new FindCommand().execute(null, "CS2"));
    }
}
