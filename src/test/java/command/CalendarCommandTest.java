package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Tests that the calendar command shows one day's bookings in time order and never changes the list.
 */
class CalendarCommandTest {
    private static final LocalDate OCTOBER_5 = LocalDate.of(2026, 10, 5);

    private String runCalendar(BookingList bookings, LocalDate date) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            new CalendarCommand().execute(bookings, date);
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void execute_bookingsOnDate_printsOnlyThatDayEarliestFirst() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Afternoon Exam", "LT1", "05/10/2026 14:00"));
        bookings.add(new Booking("lecture", "Morning Lecture", "LT2", "05/10/2026 09:00"));
        bookings.add(new Booking("exam", "Next Day Exam", "LT3", "06/10/2026 10:00"));

        String response = runCalendar(bookings, OCTOBER_5);

        assertTrue(response.contains("Calendar for 05/10/2026:"), response);
        assertTrue(response.contains("1. 09:00 - lecture - Morning Lecture (venue: LT2)"), response);
        assertTrue(response.contains("2. 14:00 - exam - Afternoon Exam (venue: LT1)"), response);
        assertFalse(response.contains("Next Day Exam"), response);
        assertEquals(3, bookings.size());
    }

    @Test
    void execute_noBookingsOnDate_printsNoBookingsMessage() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Other Day Exam", "LT1", "06/10/2026 14:00"));
        bookings.add(new Booking("exam", "Older Booking", "LT2", "10:30am"));

        String response = runCalendar(bookings, OCTOBER_5);

        assertTrue(response.contains("No bookings on 05/10/2026."), response);
        assertFalse(response.contains("Older Booking"), response);
        assertEquals(2, bookings.size());
    }

    @Test
    void execute_invalidInput_failsPrecondition() {
        BookingList bookings = new BookingList();

        assertThrows(AssertionError.class, () -> new CalendarCommand().execute(null, OCTOBER_5));
        assertThrows(AssertionError.class, () -> new CalendarCommand().execute(bookings, (LocalDate) null));
        assertThrows(AssertionError.class, () -> new CalendarCommand().execute(bookings, "tomorrow"));
    }
}
