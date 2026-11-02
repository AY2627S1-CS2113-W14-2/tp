package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;

/** Tests the view command's compatibility overload and matching output. */
class ViewCommandTest {
    private static final System.Logger LOGGER = System.getLogger(ViewCommandTest.class.getName());

    @Test
    void execute_withoutSortOption_preservesMatchingBookingOrder() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "05/10/2026 14:00"));
        bookings.add(new Booking("lecture", "Other Booking", "LT2", "05/10/2026 08:00"));
        bookings.add(new Booking("lecture", "CS2113 Midterm", "LT3", "05/10/2026 09:00"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            new ViewCommand().execute(bookings, "CS2113 Midterm");
        } finally {
            System.setOut(originalOutput);
        }

        String response = output.toString(StandardCharsets.UTF_8);
        int firstBooking = response.indexOf("Booking timing: 05/10/2026 14:00");
        int secondBooking = response.indexOf("Booking timing: 05/10/2026 09:00");
        assertTrue(firstBooking >= 0, response);
        assertTrue(secondBooking > firstBooking, response);
        assertEquals(3, bookings.size());
    }

    @Test
    void execute_withTimeSort_displaysMatchesChronologicallyWithoutChangingStoredOrder() {
        Booking latest = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");
        Booking unrelated = new Booking("lecture", "Other", "LT2", "05/10/2026 08:00");
        Booking earliest = new Booking("exam", "Midterm", "LT3", "05/10/2026 09:00");
        BookingList bookings = new BookingList();
        bookings.add(latest);
        bookings.add(unrelated);
        bookings.add(earliest);

        LOGGER.log(System.Logger.Level.INFO, "Checking chronological view sorting preserves the stored list.");
        String response = captureOutput(() -> new ViewCommand().execute(bookings, "Midterm", true));

        int firstTime = response.indexOf("Booking timing: 05/10/2026 09:00");
        int secondTime = response.indexOf("Booking timing: 05/10/2026 14:00");
        assertTrue(firstTime >= 0, response);
        assertTrue(secondTime > firstTime, response);
        assertEquals(3, bookings.size());
        assertSame(latest, bookings.get(0));
        assertSame(unrelated, bookings.get(1));
        assertSame(earliest, bookings.get(2));
    }

    @Test
    void execute_withTimeSortAcrossDates_numbersResultsAndPrintsEveryDisplayedField() {
        Booking nextDay = new Booking("lecture", "Midterm", "LT2", "06/10/2026 08:00");
        Booking laterToday = new Booking("exam", "Midterm", "LT3", "05/10/2026 15:00");
        BookingList bookings = new BookingList();
        bookings.add(nextDay);
        bookings.add(laterToday);

        LOGGER.log(System.Logger.Level.INFO, "Checking date-boundary sorting, numbering and displayed fields.");
        String response = captureOutput(() -> new ViewCommand().execute(bookings, "Midterm", true));

        assertTrue(response.indexOf("Booking 1:") < response.indexOf("Booking 2:"), response);
        assertTrue(response.indexOf("Booking purpose: exam") < response.indexOf("Booking purpose: lecture"), response);
        assertTrue(response.indexOf("Booking venue: LT3") < response.indexOf("Booking venue: LT2"), response);
        assertTrue(response.indexOf("Booking timing: 05/10/2026 15:00")
                < response.indexOf("Booking timing: 06/10/2026 08:00"), response);
        assertSame(nextDay, bookings.get(0));
        assertSame(laterToday, bookings.get(1));
    }

    @Test
    void execute_withTimeSort_keepsOriginalOrderForEqualStartTimes() {
        Booking first = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");
        Booking second = new Booking("lecture", "Midterm", "LT2", "05/10/2026 14:00");
        BookingList bookings = new BookingList();
        bookings.add(first);
        bookings.add(second);

        String response = captureOutput(() -> new ViewCommand().execute(bookings, "Midterm", true));

        assertTrue(response.indexOf("Booking venue: LT1") < response.indexOf("Booking venue: LT2"), response);
        assertSame(first, bookings.get(0));
        assertSame(second, bookings.get(1));
    }

    @Test
    void execute_sortWithLegacyTiming_reportsErrorAndPreservesStoredOrder() {
        Booking valid = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");
        Booking legacy = new Booking("exam", "Midterm", "LT2", "14:00");
        BookingList bookings = new BookingList();
        bookings.add(valid);
        bookings.add(legacy);

        LOGGER.log(System.Logger.Level.INFO, "Checking that legacy timings cannot be time-sorted.");
        InvalidCommandException error = assertThrows(InvalidCommandException.class,
                () -> new ViewCommand().execute(bookings, "Midterm", true));

        assertTrue(error.getMessage().contains("dd/MM/yyyy HH:mm"));
        assertSame(valid, bookings.get(0));
        assertSame(legacy, bookings.get(1));
    }

    @Test
    void execute_unknownName_reportsHelpfulError() {
        LOGGER.log(System.Logger.Level.INFO, "Checking the no-match response for a booking lookup.");
        InvalidCommandException error = assertThrows(InvalidCommandException.class,
                () -> new ViewCommand().execute(new BookingList(), "Missing Booking", false));

        assertEquals("No booking found with the name: Missing Booking", error.getMessage());
    }

    @Test
    void execute_missingVenueOrTiming_failsBookingInvariant() {
        Booking missingVenue = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00") {
            @Override
            public String getVenue() {
                return null;
            }
        };
        BookingList venueBookings = new BookingList();
        venueBookings.add(missingVenue);
        Booking missingTiming = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00") {
            @Override
            public String getTiming() {
                return null;
            }
        };
        BookingList timingBookings = new BookingList();
        timingBookings.add(missingTiming);

        LOGGER.log(System.Logger.Level.INFO, "Checking required venue and timing invariants.");
        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(venueBookings, "Midterm", false));
        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(timingBookings, "Midterm", false));
    }

    @Test
    void execute_nullBookingList_failsPrecondition() {
        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute((BookingList) null, "CS2113 Midterm"));
    }

    @Test
    void execute_blankName_failsPrecondition() {
        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(new BookingList(), " "));
    }

    @Test
    void execute_nullName_failsPrecondition() {
        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(new BookingList(), null));
    }

    @Test
    void execute_bookingListReturnsNonMatchingBooking_failsInvariant() {
        BookingList bookings = new BookingList() {
            @Override
            public ArrayList<Booking> findAllByName(String name) {
                return new ArrayList<>(List.of(
                        new Booking("exam", "Different Name", "LT1", "05/10/2026 14:00")));
            }
        };

        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(bookings, "CS2113 Midterm", false));
    }

    @Test
    void execute_bookingListChangedDuringLookup_failsReadOnlyInvariant() {
        BookingList bookings = new BookingList() {
            @Override
            public ArrayList<Booking> findAllByName(String name) {
                ArrayList<Booking> matches = super.findAllByName(name);
                add(new Booking("lecture", "Other", "LT2", "05/10/2026 15:00"));
                return matches;
            }
        };
        bookings.add(new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00"));

        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(bookings, "Midterm", false));
    }

    @Test
    void execute_bookingWithMissingReason_failsInvariant() {
        Booking booking = new Booking("exam", "CS2113 Midterm", "LT1", "05/10/2026 14:00") {
            @Override
            public String getReason() {
                return null;
            }
        };
        BookingList bookings = new BookingList();
        bookings.add(booking);

        assertThrows(AssertionError.class,
                () -> new ViewCommand().execute(bookings, "CS2113 Midterm", false));
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            action.run();
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
