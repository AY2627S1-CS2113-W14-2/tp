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
    void execute_invalidArguments_failPreconditions() {
        ListCommand command = new ListCommand();
        assertThrows(AssertionError.class, () -> command.execute(null, "bookings"));
        for (String item : new String[]{null, "", "meetings"}) {
            assertThrows(AssertionError.class, () -> command.execute(new BookingList(), item));
        }
    }
}
