package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import tasks.Booking;
import tasks.BookingList;
import exceptions.InvalidCommandException;
import ui.Parser;

/**
 * Checks command parsing, listing output and preservation of stored booking order.
 *
 * @author Kytersaurus
 */
class ListCommandTest {
    @Test
    void execute_fineLogging_recordsSelectionResultsAndRejectedTimeSorting() {
        Logger logger = Logger.getLogger(ListCommand.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalParentHandlers = logger.getUseParentHandlers();
        List<LogRecord> records = new ArrayList<>();
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
        try {
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.FINE);
            logger.addHandler(handler);
            BookingList bookings = new BookingList();
            bookings.add(new Booking("exam", "Legacy", "LT19", "10AM"));
            ListCommand command = new ListCommand(new Parser());

            command.execute(bookings, "list r/exams s/alpha");
            assertThrows(InvalidCommandException.class, () -> command.execute(bookings, "list s/time"));

            assertTrue(records.stream().anyMatch(record -> record.getMessage().contains(
                    "filter=exams, sort=ALPHABETIC, stored count=1")));
            assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("displayed count=1")));
            assertTrue(records.stream().anyMatch(record -> record.getMessage().contains("Time sorting rejected")));
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalParentHandlers);
            handler.close();
        }
    }

    @Test
    void execute_bookingProviderReturnsNull_failsBeforeDisplayingInvalidBooking() {
        BookingList bookings = new BookingList() {
            @Override
            public Booking get(int index) {
                return null;
            }
        };
        bookings.add(new Booking("exam", "Existing", "LT19", "28/10/2026 14:00"));

        AssertionError error = assertThrows(AssertionError.class,
                () -> new ListCommand(new Parser()).execute(bookings, "list"));

        assertEquals("No such booking exists at index 0", error.getMessage());
        assertTrue(ListCommand.lastShownList.isEmpty());
    }

    @Test
    void execute_bookingProviderRemovesBooking_failsReadOnlyInvariant() {
        BookingList bookings = new BookingList() {
            @Override
            public Booking get(int index) {
                Booking result = super.get(index);
                remove(index);
                return result;
            }
        };
        bookings.add(new Booking("exam", "Existing", "LT19", "28/10/2026 14:00"));

        AssertionError error = assertThrows(AssertionError.class,
                () -> new ListCommand(new Parser()).execute(bookings, "list"));

        assertEquals("Listing bookings should not change list size: before: 1, after 0", error.getMessage());
    }

    @Test
    void execute_sortUnexpectedlyRemovesBooking_failsSizeInvariant() {
        ArrayList<Booking> originalDisplay = ListCommand.lastShownList;
        ListCommand.lastShownList = new ArrayList<>() {
            @Override
            public void sort(Comparator<? super Booking> comparator) {
                super.sort(comparator);
                removeLast();
            }
        };
        try {
            BookingList bookings = new BookingList();
            bookings.add(new Booking("exam", "Existing", "LT19", "28/10/2026 14:00"));

            AssertionError error = assertThrows(AssertionError.class,
                    () -> new ListCommand(new Parser()).execute(bookings, "list s/alpha"));

            assertEquals("Sorting the list should not change the number of bookings: before: 1 after: 0",
                    error.getMessage());
            assertEquals(1, bookings.size());
        } finally {
            ListCommand.lastShownList = originalDisplay;
        }
    }

    @Test
    void execute_sortUnexpectedlyIntroducesNull_failsPrintPrecondition() {
        ArrayList<Booking> originalDisplay = ListCommand.lastShownList;
        ListCommand.lastShownList = new ArrayList<>() {
            @Override
            public void sort(Comparator<? super Booking> comparator) {
                super.sort(comparator);
                set(0, null);
            }
        };
        try {
            BookingList bookings = new BookingList();
            Booking existing = new Booking("exam", "Existing", "LT19", "28/10/2026 14:00");
            bookings.add(existing);

            AssertionError error = assertThrows(AssertionError.class,
                    () -> new ListCommand(new Parser()).execute(bookings, "list s/alpha"));

            assertEquals("Booking to be printed cannot be null", error.getMessage());
            assertSame(existing, bookings.get(0));
        } finally {
            ListCommand.lastShownList = originalDisplay;
        }
    }

    @AfterEach
    void clearLastShownList() {
        ListCommand.lastShownList.clear();
    }

    @Test
    void execute_defaultOverload_showsOriginalOrder() {
        BookingList bookings = new BookingList();
        Booking zebra = new Booking("exam", "Zebra", "LT1", "10AM");
        Booking alpha = new Booking("lecture", "alpha", "LT2", "11AM");
        bookings.add(zebra);
        bookings.add(alpha);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            new ListCommand(new Parser()).execute(bookings, "list");
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals("Ok, here is the list of bookings in ORIGINAL order\n"
                + "1. exam Title: Zebra\n2. lecture Title: alpha\n",
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        assertEquals(List.of(zebra, alpha), ListCommand.lastShownList);
    }

    @Test
    void execute_sortingChangesOnlyDisplayedCopyAndNextCallClearsIt() {
        BookingList bookings = new BookingList();
        Booking zebra = new Booking("exam", "Zebra", "LT1", "10AM");
        Booking alpha = new Booking("lecture", "alpha", "LT2", "11AM");
        bookings.add(zebra);
        bookings.add(alpha);
        ListCommand command = new ListCommand(new Parser());

        command.execute(bookings, "bookings", SortOrder.ALPHABETIC);

        assertEquals(List.of(alpha, zebra), ListCommand.lastShownList);
        assertSame(zebra, bookings.get(0));
        assertSame(alpha, bookings.get(1));
        command.execute(new BookingList(), "list");
        assertEquals(List.of(), ListCommand.lastShownList);
    }

    @Test
    void execute_timeIncreasing_ordersByDateThenTimeWithoutChangingStoredOrder() {
        BookingList bookings = createTimedBookings();
        Booking newYear = bookings.get(0);
        Booking afternoon = bookings.get(1);
        Booking morning = bookings.get(2);
        Booking nextMonth = bookings.get(3);

        new ListCommand(new Parser()).execute(bookings, "bookings", SortOrder.TIME_INCREASING);

        assertEquals(List.of(morning, afternoon, newYear, nextMonth), ListCommand.lastShownList);
        assertSame(newYear, bookings.get(0));
        assertSame(afternoon, bookings.get(1));
        assertSame(morning, bookings.get(2));
        assertSame(nextMonth, bookings.get(3));
        assertEquals(4, bookings.size());
    }

    @Test
    void execute_timeDecreasing_ordersByDateThenTimeWithoutChangingStoredOrder() {
        BookingList bookings = createTimedBookings();
        Booking newYear = bookings.get(0);
        Booking afternoon = bookings.get(1);
        Booking morning = bookings.get(2);
        Booking nextMonth = bookings.get(3);

        new ListCommand(new Parser()).execute(bookings, "bookings", SortOrder.TIME_DECREASING);

        assertEquals(List.of(nextMonth, newYear, afternoon, morning), ListCommand.lastShownList);
        assertSame(newYear, bookings.get(0));
        assertSame(afternoon, bookings.get(1));
        assertSame(morning, bookings.get(2));
        assertSame(nextMonth, bookings.get(3));
        assertEquals(4, bookings.size());
    }

    @Test
    void execute_timeSorting_filtersBeforeSorting() {
        BookingList bookings = createTimedBookings();
        Booking newYear = bookings.get(0);
        Booking morning = bookings.get(2);
        ListCommand command = new ListCommand(new Parser());

        command.execute(bookings, "exams", SortOrder.TIME_INCREASING);
        assertEquals(List.of(morning, newYear), ListCommand.lastShownList);

        command.execute(bookings, "exams", SortOrder.TIME_DECREASING);
        assertEquals(List.of(newYear, morning), ListCommand.lastShownList);
        assertEquals(4, bookings.size());
    }

    @Test
    void execute_equalStartTimes_preservesInsertionOrderInBothDirections() {
        BookingList bookings = new BookingList();
        Booking zebra = new Booking("exam", "Zebra", "LT1", "25/10/2026 09:00");
        Booking alpha = new Booking("exam", "Alpha", "LT2", "25/10/2026 09:00");
        bookings.add(zebra);
        bookings.add(alpha);

        for (SortOrder order : new SortOrder[]{SortOrder.TIME_INCREASING, SortOrder.TIME_DECREASING}) {
            new ListCommand(new Parser()).execute(bookings, "bookings", order);
            assertEquals(List.of(zebra, alpha), ListCommand.lastShownList, order.toString());
        }
    }

    @Test
    void execute_timeSorting_handlesEmptyAndSingleBookingLists() {
        Booking booking = new Booking("lecture", "Only booking", "LT1", "25/10/2026 09:00");
        ListCommand command = new ListCommand(new Parser());

        for (SortOrder order : new SortOrder[]{SortOrder.TIME_INCREASING, SortOrder.TIME_DECREASING}) {
            command.execute(new BookingList(), "bookings", order);
            assertEquals(List.of(), ListCommand.lastShownList, order.toString());

            BookingList bookings = new BookingList();
            bookings.add(booking);
            command.execute(bookings, "bookings", order);
            assertEquals(List.of(booking), ListCommand.lastShownList, order.toString());
        }
    }

    @Test
    void execute_timeSorting_legacyTimingReportsUsefulError() {
        BookingList bookings = new BookingList();
        Booking legacy = new Booking("exam", "Old booking", "LT1", "10AM");
        Booking dated = new Booking("exam", "Dated booking", "LT2", "25/10/2026 09:00");
        bookings.add(legacy);
        bookings.add(dated);

        for (SortOrder order : new SortOrder[]{SortOrder.TIME_INCREASING, SortOrder.TIME_DECREASING}) {
            InvalidCommandException error = assertThrows(InvalidCommandException.class,
                    () -> new ListCommand(new Parser()).execute(bookings, "bookings", order));
            assertEquals("Cannot sort these bookings: their timing must use dd/MM/yyyy HH:mm",
                    error.getMessage());
            assertSame(legacy, bookings.get(0));
            assertSame(dated, bookings.get(1));
        }
    }

    @Test
    void execute_timeSorting_ignoresLegacyBookingsOutsideRequestedFilter() {
        BookingList bookings = new BookingList();
        Booking legacy = new Booking("lecture", "Old lecture", "LT1", "10AM");
        Booking dated = new Booking("exam", "Dated exam", "LT2", "25/10/2026 09:00");
        bookings.add(legacy);
        bookings.add(dated);

        for (SortOrder order : new SortOrder[]{SortOrder.TIME_INCREASING, SortOrder.TIME_DECREASING}) {
            new ListCommand(new Parser()).execute(bookings, "exams", order);

            assertEquals(List.of(dated), ListCommand.lastShownList, order.toString());
            assertSame(legacy, bookings.get(0));
            assertSame(dated, bookings.get(1));
        }
    }

    /** Creates deliberately unordered bookings across times, months and a year boundary. */
    private BookingList createTimedBookings() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "New Year", "LT1", "01/01/2027 08:00"));
        bookings.add(new Booking("lecture", "Afternoon", "LT2", "31/12/2026 16:00"));
        bookings.add(new Booking("exam", "Morning", "LT3", "31/12/2026 09:00"));
        bookings.add(new Booking("lecture", "Next month", "LT4", "01/02/2027 07:00"));
        return bookings;
    }

    @Test
    void execute_invalidArguments_failPreconditions() {
        ListCommand command = new ListCommand(new Parser());
        assertThrows(AssertionError.class, () -> new ListCommand(null));
        assertThrows(AssertionError.class, () -> command.execute(null, "list"));
        assertThrows(AssertionError.class, () -> command.execute(null, "bookings", SortOrder.ORIGINAL));
        assertThrows(AssertionError.class, () -> command.execute(new BookingList(), null));
        for (String item : new String[]{null, "", "meetings"}) {
            assertThrows(AssertionError.class,
                    () -> command.execute(new BookingList(), item, SortOrder.ORIGINAL));
        }
    }

    @Test
    void execute_invalidCommandOptions_preservesBookingsAndPreviousDisplay() {
        BookingList bookings = createTimedBookings();
        ListCommand command = new ListCommand(new Parser());
        command.execute(bookings, "list r/exams s/time");
        List<Booking> previousDisplay = List.copyOf(ListCommand.lastShownList);

        for (String input : new String[]{"", "bookings", "list r/meetings",
            "list s/invalid", "list r/exams r/lectures", "list s/alpha s/time"}) {
            assertThrows(InvalidCommandException.class, () -> command.execute(bookings, input), input);
            assertEquals(previousDisplay, ListCommand.lastShownList, input);
            assertEquals(4, bookings.size(), input);
        }
    }
}
