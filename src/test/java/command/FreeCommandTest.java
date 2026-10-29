package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import tasks.VenueCatalog;
import ui.Parser;

/** Checks one-hour venue searches, printed numbering and preservation of bookings. */
class FreeCommandTest {
    private static final LocalDateTime REQUESTED_TIME = LocalDateTime.of(2026, 10, 28, 14, 0);
    private final FreeCommand command = new FreeCommand(new Parser());

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
