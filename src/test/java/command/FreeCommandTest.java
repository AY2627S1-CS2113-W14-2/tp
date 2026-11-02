package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import tasks.VenueCatalog;
import ui.Parser;

/**
 * Checks one-hour venue searches, printed numbering and preservation of bookings.
 *
 * @author Kytersaurus
 */
class FreeCommandTest {
    private static final LocalDateTime REQUESTED_TIME = LocalDateTime.of(2026, 10, 28, 14, 0);
    private final FreeCommand command = new FreeCommand(new Parser());

    @Test
    void execute_fineLogging_recordsBothSearchModesAndRejectionWithoutChangingConsoleOutput() {
        BookingList bookings = new BookingList();
        List<LogRecord> records = new ArrayList<>();

        String output = captureOutput(() -> captureFineLogs(records, () -> {
            command.execute(bookings, "free t/28/10/2026 14:00");
            command.execute(bookings, "free v/LT19 d/28/10/2026");
            assertThrows(InvalidCommandException.class,
                    () -> command.execute(bookings, "free v/UNKNOWN d/28/10/2026"));
        }));

        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("mode=venues")
                && record.getMessage().contains("start=2026-10-28T14:00")));
        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("mode=slots")
                && record.getMessage().contains("venue=LT19")
                && record.getMessage().contains("date=2026-10-28")));
        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("mode=venues")
                && record.getMessage().contains("result count=")));
        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("mode=slots")
                && record.getMessage().contains("result count=24")));
        assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("rejected")
                && record.getMessage().contains("UNKNOWN")));
        assertTrue(records.stream().allMatch(record -> record.getLevel().equals(Level.FINE)));
        assertTrue(output.contains("Available venues from 28/10/2026 14:00 to 28/10/2026 15:00:"));
        assertTrue(output.contains("Available one-hour slots for LT19 on 28/10/2026"));
        assertFalse(output.contains("Availability search"));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void execute_repeatedSearches_keepVenueDateAndModeResultsIndependent() {
        BookingList bookings = new BookingList();
        Booking firstDay = new Booking("exam", "First day", "LT19", "28/10/2026 14:00");
        Booking nextDay = new Booking("lecture", "Next day", "LT17", "29/10/2026 16:00");
        bookings.add(firstDay);
        bookings.add(nextDay);

        String firstSearch = captureOutput(() -> command.execute(bookings, "free v/LT19 d/28/10/2026"));
        assertEquals(23, firstSearch.lines().filter(line -> line.matches("\\d+\\. .* to .*")).count());
        assertFalse(firstSearch.contains(". 28/10/2026 14:00 to"));

        String nextDate = captureOutput(() -> command.execute(bookings, "free v/LT19 d/29/10/2026"));
        assertEquals(24, nextDate.lines().filter(line -> line.matches("\\d+\\. .* to .*")).count());
        assertTrue(nextDate.contains(". 29/10/2026 16:00 to 29/10/2026 17:00"));

        String nextVenue = captureOutput(() -> command.execute(bookings, "free v/LT17 d/29/10/2026"));
        assertEquals(23, nextVenue.lines().filter(line -> line.matches("\\d+\\. .* to .*")).count());
        assertFalse(nextVenue.contains(". 29/10/2026 16:00 to"));

        List<String> expectedVenues = new ArrayList<>(
                VenueCatalog.findAvailableVenues(new BookingList(), REQUESTED_TIME));
        expectedVenues.remove("LT19");
        String timeSearch = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 14:00"));
        assertEquals(expectedOutput(expectedVenues), timeSearch);
        assertEquals(2, bookings.size());
        assertSame(firstDay, bookings.get(0));
        assertSame(nextDay, bookings.get(1));
    }

    @Test
    void execute_venueSearchAcrossYearEnd_printsNextYearAndSkipsPreviousDayOverlap() {
        BookingList bookings = new BookingList();
        Booking previousDay = new Booking("exam", "Previous day", "lt19", "30/12/2026 23:30");
        bookings.add(previousDay);

        String output = captureOutput(() -> command.execute(bookings, "free v/LT19 d/31/12/2026"));

        assertTrue(output.startsWith("Available one-hour slots for LT19 on 31/12/2026"));
        assertFalse(output.contains(". 31/12/2026 00:00 to"));
        assertTrue(output.contains("1. 31/12/2026 01:00 to 31/12/2026 02:00\n"));
        assertTrue(output.endsWith("23. 31/12/2026 23:00 to 01/01/2027 00:00\n"));
        assertEquals(23, output.lines().filter(line -> line.matches("\\d+\\. .* to .*")).count());
        assertEquals(1, bookings.size());
        assertSame(previousDay, bookings.get(0));
    }

    @Test
    void execute_onlyOneSlotAvailable_numbersItFromOne() {
        BookingList bookings = new BookingList();
        for (int hour = 0; hour < 24; hour++) {
            if (hour != 14) {
                bookings.add(new Booking("exam", "Hour " + hour, "LT19",
                        "28/10/2026 " + String.format(java.util.Locale.ROOT, "%02d:00", hour)));
            }
        }

        String output = captureOutput(() -> command.execute(bookings, "free v/LT19 d/28/10/2026"));

        assertEquals("Available one-hour slots for LT19 on 28/10/2026 (starts on the hour):\n"
                + "1. 28/10/2026 14:00 to 28/10/2026 15:00\n", output);
        assertEquals(23, bookings.size());
    }

    @Test
    void execute_venueSearch_printsNumberedSlotsAndNextDayEndWithoutChangingBookings() {
        BookingList bookings = new BookingList();
        Booking existing = new Booking("exam", "Existing", "LT19", "28/10/2026 14:30");
        bookings.add(existing);
        StringBuilder expected = new StringBuilder(
                "Available one-hour slots for LT19 on 28/10/2026 (starts on the hour):\n");
        int number = 1;
        for (int hour = 0; hour < 24; hour++) {
            if (hour == 14 || hour == 15) {
                continue;
            }
            String end = hour == 23 ? "29/10/2026 00:00"
                    : "28/10/2026 " + String.format(java.util.Locale.ROOT, "%02d:00", hour + 1);
            expected.append(number++).append(". 28/10/2026 ")
                    .append(String.format(java.util.Locale.ROOT, "%02d:00", hour))
                    .append(" to ").append(end).append('\n');
        }

        String output = captureOutput(() -> command.execute(bookings, "free v/LT19 d/28/10/2026"));

        assertEquals(expected.toString(), output);
        assertEquals(1, bookings.size());
        assertSame(existing, bookings.get(0));
    }

    @Test
    void execute_venueFullyOccupied_printsNoSlotsMessage() {
        BookingList bookings = new BookingList();
        for (int hour = 0; hour < 24; hour++) {
            bookings.add(new Booking("exam", "Hour " + hour, "LT19",
                    "28/10/2026 " + String.format(java.util.Locale.ROOT, "%02d:00", hour)));
        }
        String output = captureOutput(() -> command.execute(bookings, "free v/LT19 d/28/10/2026"));
        assertEquals("Available one-hour slots for LT19 on 28/10/2026 (starts on the hour):\n"
                + "No one-hour slots are available\n", output);
        assertEquals(24, bookings.size());
    }

    @Test
    void execute_invalidVenueSearch_throwsWithoutPrintingResults() {
        BookingList bookings = new BookingList();
        for (String input : new String[]{"free v/UNKNOWN d/28/10/2026", "free v/LT19 d/31/02/2026"}) {
            String output = captureOutput(() -> assertThrows(InvalidCommandException.class,
                    () -> command.execute(bookings, input)));
            assertEquals("", output, input);
        }
        assertEquals(0, bookings.size());
    }

    @Test
    void execute_emptyBookings_printsAllPresetVenuesWithConsecutiveNumbers() {
        BookingList bookings = new BookingList();
        List<String> venues = VenueCatalog.findAvailableVenues(bookings, REQUESTED_TIME);

        String output = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 14:00"));

        assertEquals(expectedOutput(venues), output);
        assertEquals(0, bookings.size());
    }

    @Test
    void execute_occupiedVenue_excludesItAndPreservesStoredBookings() {
        BookingList bookings = new BookingList();
        Booking occupied = new Booking("exam", "Occupied", "lt19", "28/10/2026 14:00", 80);
        Booking anotherTime = new Booking("lecture", "Other time", "LT17", "28/10/2026 14:30");
        bookings.add(occupied);
        bookings.add(anotherTime);
        List<String> expectedVenues = new ArrayList<>(
                VenueCatalog.findAvailableVenues(new BookingList(), REQUESTED_TIME));
        expectedVenues.remove("LT19");
        expectedVenues.remove("LT17");

        String output = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 14:00"));

        assertEquals(expectedOutput(expectedVenues), output);
        assertEquals(2, bookings.size());
        assertSame(occupied, bookings.get(0));
        assertSame(anotherTime, bookings.get(1));
        assertEquals(80, occupied.getAttendeeCount());
    }

    @Test
    void execute_allVenuesOccupied_printsNoAvailabilityMessage() {
        BookingList bookings = new BookingList();
        List<String> venues = VenueCatalog.findAvailableVenues(bookings, REQUESTED_TIME);
        for (String venue : venues) {
            bookings.add(new Booking("exam", "Booking at " + venue, venue, "28/10/2026 14:00"));
        }

        String output = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 14:00"));

        assertEquals("Available venues from 28/10/2026 14:00 to 28/10/2026 15:00:\n"
                + "No venues are available at specified time\n", output);
        assertEquals(venues.size(), bookings.size());
    }

    @Test
    void execute_oneHourSlots_excludesOverlapsAndAllowsTouchingBoundaries() {
        for (String timing : new String[]{"28/10/2026 13:00", "28/10/2026 13:01",
            "28/10/2026 14:00", "28/10/2026 14:59", "28/10/2026 15:00"}) {
            BookingList bookings = new BookingList();
            Booking existing = new Booking("exam", "Existing", "lt19", timing);
            bookings.add(existing);
            List<String> expectedVenues = new ArrayList<>(
                    VenueCatalog.findAvailableVenues(new BookingList(), REQUESTED_TIME));
            if (!timing.equals("28/10/2026 13:00") && !timing.equals("28/10/2026 15:00")) {
                expectedVenues.remove("LT19");
            }

            String output = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 14:00"));

            assertEquals(expectedOutput(expectedVenues), output, timing);
            assertEquals(1, bookings.size(), timing);
            assertSame(existing, bookings.get(0), timing);
        }
    }

    @Test
    void execute_slotCrossingMidnight_excludesNextDayOverlapAndPrintsEndDate() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Next day", "LT19", "29/10/2026 00:00"));
        List<String> expectedVenues = new ArrayList<>(VenueCatalog.findAvailableVenues(
                new BookingList(), LocalDateTime.of(2026, 10, 28, 23, 30)));
        expectedVenues.remove("LT19");

        String output = captureOutput(() -> command.execute(bookings, "free t/28/10/2026 23:30"));

        String expected = expectedOutput(expectedVenues).replace(
                "28/10/2026 14:00 to 28/10/2026 15:00", "28/10/2026 23:30 to 29/10/2026 00:30");
        assertEquals(expected, output);
        assertEquals(1, bookings.size());
    }

    @Test
    void execute_invalidInput_throwsWithoutPrintingResults() {
        BookingList bookings = new BookingList();
        String output = captureOutput(() -> assertThrows(InvalidCommandException.class,
                () -> command.execute(bookings, "free t/31/02/2026 14:00")));

        assertEquals("", output);
        assertEquals(0, bookings.size());
    }

    @Test
    void constructorAndExecute_nullDependencies_failPreconditions() {
        assertThrows(AssertionError.class, () -> new FreeCommand(null));
        assertThrows(AssertionError.class,
                () -> command.execute(null, "free t/28/10/2026 14:00"));
        assertThrows(AssertionError.class, () -> command.execute(new BookingList(), null));
    }

    /** Captures FINE records and restores logger settings even if command execution fails. */
    private void captureFineLogs(List<LogRecord> records, Runnable action) {
        Logger logger = Logger.getLogger(FreeCommand.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalParentHandlers = logger.getUseParentHandlers();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        handler.setLevel(Level.FINE);
        logger.addHandler(handler);
        try {
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
            action.run();
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalParentHandlers);
            handler.close();
        }
    }

    /** Builds the expected numbered output without duplicating the preset venue catalogue. */
    private String expectedOutput(List<String> venues) {
        StringBuilder expected = new StringBuilder(
                "Available venues from 28/10/2026 14:00 to 28/10/2026 15:00:\n");
        for (int i = 0; i < venues.size(); i++) {
            expected.append(i + 1).append(". ").append(venues.get(i)).append('\n');
        }
        return expected.toString();
    }

    /** Captures command output and restores the shared output stream even if an assertion fails. */
    private String captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            action.run();
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
