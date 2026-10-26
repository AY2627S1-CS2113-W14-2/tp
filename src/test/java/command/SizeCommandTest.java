package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;

/** Checks that checksize reports the maximum recorded attendee count for each venue. */
class SizeCommandTest {
    @Test
    void execute_checksizePrintsPerVenueMaximumAndIgnoresUnknownCounts() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "First LT1", "LT1", "05/10/2026 09:00", 40));
        bookings.add(new Booking("exam", "LT2 Booking", "LT2", "05/10/2026 10:00", 65));
        bookings.add(new Booking("lecture", "Largest LT1", "lt1", "05/10/2026 11:00", 80));
        bookings.add(new Booking("exam", "No Count", "LT3", "05/10/2026 12:00"));
        bookings.add(new Booking("lecture", "Smaller LT2", "LT2", "05/10/2026 13:00", 25));

        String output = captureOutput(() -> new SizeCommand().execute(bookings, "checksize"));

        assertEquals("Largest recorded attendee count by venue:\n"
                        + "LT1: 80 attendees\n"
                        + "LT2: 65 attendees\n",
                normalizeNewlines(output));
    }

    @Test
    void execute_checksizeWithNoRecordedCountsReportsEmptyResult() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "No Count", "LT1", "05/10/2026 09:00"));

        String output = captureOutput(() -> new SizeCommand().execute(bookings, "checksize"));

        assertEquals("No bookings include an attendee count.\n", normalizeNewlines(output));
    }

    @Test
    void execute_wrongSyntaxThrowsHelpfulError() {
        InvalidCommandException error = assertThrows(InvalidCommandException.class,
                () -> new SizeCommand().execute(new BookingList(), "checksize a/80"));

        assertEquals("Please use: checksize", error.getMessage());
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            action.run();
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    private String normalizeNewlines(String output) {
        return output.replace("\r\n", "\n");
    }
}
