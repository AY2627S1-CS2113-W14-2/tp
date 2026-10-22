package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;

/** Tests the view command's compatibility overload and matching output. */
class ViewCommandTest {
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
}
