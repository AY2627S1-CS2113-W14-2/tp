package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;
import Exceptions.InvalidCommandException;

/** Checks the default list overload and ensures sorting does not reorder stored bookings. */
class ListCommandTest {
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
            new ListCommand().execute(bookings, "bookings");
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals("1. exam Title: Zebra\n2. lecture Title: alpha\n",
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
        ListCommand command = new ListCommand();

        command.execute(bookings, "bookings", SortOrder.ALPHABETIC);

        assertEquals(List.of(alpha, zebra), ListCommand.lastShownList);
        assertSame(zebra, bookings.get(0));
        assertSame(alpha, bookings.get(1));
        command.execute(new BookingList(), "bookings");
        assertEquals(List.of(), ListCommand.lastShownList);
    }

    @Test
    void execute_timeIncreasing_ordersByDateThenTimeWithoutChangingStoredOrder() {
        BookingList bookings = createTimedBookings();
        Booking newYear = bookings.get(0);
        Booking afternoon = bookings.get(1);
        Booking morning = bookings.get(2);
        Booking nextMonth = bookings.get(3);

        new ListCommand().execute(bookings, "bookings", SortOrder.TIME_INCREASING);

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

        new ListCommand().execute(bookings, "bookings", SortOrder.TIME_DECREASING);

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
        ListCommand command = new ListCommand();

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
            new ListCommand().execute(bookings, "bookings", order);
            assertEquals(List.of(zebra, alpha), ListCommand.lastShownList, order.toString());
        }
    }

    @Test
    void execute_timeSorting_handlesEmptyAndSingleBookingLists() {
        Booking booking = new Booking("lecture", "Only booking", "LT1", "25/10/2026 09:00");
        ListCommand command = new ListCommand();

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
                    () -> new ListCommand().execute(bookings, "bookings", order));
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
            new ListCommand().execute(bookings, "exams", order);

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
        ListCommand command = new ListCommand();
        assertThrows(AssertionError.class, () -> command.execute(null, "bookings"));
        for (String item : new String[]{null, "", "meetings"}) {
            assertThrows(AssertionError.class, () -> command.execute(new BookingList(), item));
        }
    }
}
