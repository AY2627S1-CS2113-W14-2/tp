package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;

/** Checks that checksize reports the maximum recorded attendee count for each venue. */
class SizeCommandTest {
    private static final System.Logger LOGGER = System.getLogger(SizeCommandTest.class.getName());

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
    void execute_emptyBookingList_reportsNoAttendeeCounts() {
        LOGGER.log(System.Logger.Level.INFO, "Checking empty-booking output for attendee counts.");
        String output = captureOutput(() -> new SizeCommand().execute(new BookingList(), "checksize"));

        assertEquals("No bookings include an attendee count.\n", normalizeNewlines(output));
    }

    @Test
    void execute_unsortedCaseVariants_groupsCountsAndPrintsVenuesAlphabetically() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Zulu Booking", "LTZ", "05/10/2026 09:00", 30));
        bookings.add(new Booking("lecture", "Alpha Booking", "lta", "05/10/2026 10:00", 45));
        bookings.add(new Booking("exam", "Largest Alpha", "LTA", "05/10/2026 11:00", 90));

        LOGGER.log(System.Logger.Level.INFO, "Checking venue grouping, maximum selection and alphabetical output.");
        String output = captureOutput(() -> new SizeCommand().execute(bookings, "checksize"));

        assertEquals("Largest recorded attendee count by venue:\n"
                        + "lta: 90 attendees\n"
                        + "LTZ: 30 attendees\n",
                normalizeNewlines(output));
        assertEquals(3, bookings.size());
    }

    @Test
    void execute_whitespaceAndCaseAroundCommandAreAcceptedWithoutChangingBookings() {
        Booking first = new Booking("exam", "Midterm", "LT2", "05/10/2026 09:00", 30);
        Booking second = new Booking("lecture", "Final", "LT1", "05/10/2026 10:00", 50);
        BookingList bookings = new BookingList();
        bookings.add(first);
        bookings.add(second);

        LOGGER.log(System.Logger.Level.INFO, "Checking checksize command normalization and read-only behavior.");
        String output = captureOutput(() -> new SizeCommand().execute(bookings, "  CHECKSIZE  "));

        assertEquals("Largest recorded attendee count by venue:\n"
                        + "LT1: 50 attendees\n"
                        + "LT2: 30 attendees\n",
                normalizeNewlines(output));
        assertEquals(2, bookings.size());
        assertSame(first, bookings.get(0));
        assertSame(second, bookings.get(1));
    }

    @Test
    void execute_nullBookingListOrInput_failsPrecondition() {
        SizeCommand command = new SizeCommand();

        assertThrows(AssertionError.class, () -> command.execute(null, "checksize"));
        assertThrows(AssertionError.class, () -> command.execute(new BookingList(), null));
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
