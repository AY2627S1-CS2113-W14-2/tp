package tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/** Checks list ownership, search results, and venue/date boundaries. */
class BookingListTest {
    private final Booking first = new Booking("exam", "CS2113 Midterm", "LT1", "05/10/2026 14:00");
    private final Booking second = new Booking("lecture", "CS2113 Midterm", "LT2", "05/10/2026 09:00");

    @Test
    void addAndRemove_preserveOrderAndLastBooking() {
        BookingList bookings = new BookingList();
        assertTrue(bookings.isEmpty());
        assertThrows(NoSuchElementException.class, bookings::getLast);

        bookings.add(first);
        bookings.add(second);

        assertFalse(bookings.isEmpty());
        assertEquals(2, bookings.size());
        assertSame(first, bookings.get(0));
        assertSame(second, bookings.getLast());
        bookings.remove(0);
        assertEquals(1, bookings.size());
        assertSame(second, bookings.get(0));
        bookings.remove(0);
        assertTrue(bookings.isEmpty());
    }

    @Test
    void addGetAndRemove_invalidArguments_leaveListUnchanged() {
        BookingList bookings = new BookingList();
        bookings.add(first);

        assertThrows(AssertionError.class, () -> bookings.add(null));
        for (int index : new int[]{-1, 1}) {
            assertThrows(AssertionError.class, () -> bookings.get(index));
            assertThrows(AssertionError.class, () -> bookings.remove(index));
        }
        assertEquals(1, bookings.size());
        assertSame(first, bookings.getLast());
    }

    @Test
    void nameSearches_ignoreCaseAndReturnIndependentLists() {
        BookingList bookings = new BookingList();
        bookings.add(first);
        bookings.add(second);

        assertEquals(0, bookings.findByName("cs2113 MIDTERM"));
        assertEquals(-1, bookings.findByName("CS2113"));
        assertTrue(bookings.findAllByName("CS2113").isEmpty());
        assertEquals(List.of(first, second), bookings.findAllByName("cs2113 midterm"));
        assertEquals(List.of(first, second), bookings.findByKeyword("MID"));
        bookings.findAllByName("CS2113 Midterm").clear();
        bookings.findByKeyword("CS2113").clear();
        assertEquals(2, bookings.size());
    }

    @Test
    void searches_nullOrBlankArguments_failPreconditions() {
        BookingList bookings = new BookingList();
        for (String input : new String[]{null, "", " \t "}) {
            assertThrows(AssertionError.class, () -> bookings.findByName(input));
            assertThrows(AssertionError.class, () -> bookings.findAllByName(input));
            assertThrows(AssertionError.class, () -> bookings.findByKeyword(input));
        }
        assertThrows(AssertionError.class, () -> bookings.findByDate(null));
        assertThrows(AssertionError.class, () -> bookings.findClash(null));
        assertThrows(AssertionError.class,
                () -> bookings.findClash(new Booking("exam", "Legacy", "LT1", "10AM")));
    }

    @Test
    void findClash_matchesOnlySameVenueAndFullDateTime() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Legacy", "LT1", "14:00"));
        assertNull(bookings.findClash(first));
        bookings.add(first);

        assertSame(first, bookings.findClash(new Booking("lecture", "Other", "lt1", "05/10/2026 14:00")));
        assertNull(bookings.findClash(new Booking("exam", "Other", "LT2", "05/10/2026 14:00")));
        assertNull(bookings.findClash(new Booking("exam", "Other", "LT1", "05/10/2026 14:01")));
        assertNull(bookings.findClash(new Booking("exam", "Other", "LT1", "06/10/2026 14:00")));
        assertEquals(2, bookings.size());
    }

    @Test
    void isVenueAvailable_emptyListAndDifferentStartTimes_areAvailable() {
        BookingList bookings = new BookingList();
        LocalDateTime requestedTime = LocalDateTime.of(2026, 10, 5, 14, 0);
        assertTrue(bookings.isVenueAvailable("LT1", requestedTime));
        bookings.add(first);

        assertFalse(bookings.isVenueAvailable(" \tlt1 ", requestedTime));
        assertTrue(bookings.isVenueAvailable("LT2", requestedTime));
        assertTrue(bookings.isVenueAvailable("LT1", requestedTime.plusMinutes(1)));
        assertTrue(bookings.isVenueAvailable("LT1", requestedTime.plusDays(1)));
        assertEquals(1, bookings.size());
        assertSame(first, bookings.get(0));
    }

    @Test
    void isVenueAvailable_legacyTimingIsSkippedAndCancellationFreesVenue() {
        BookingList bookings = new BookingList();
        LocalDateTime requestedTime = first.getStartTime();
        bookings.add(new Booking("exam", "Legacy", "LT1", "14:00"));
        assertTrue(bookings.isVenueAvailable("LT1", requestedTime));
        bookings.add(first);
        assertFalse(bookings.isVenueAvailable("LT1", requestedTime));

        bookings.remove(1);

        assertTrue(bookings.isVenueAvailable("LT1", requestedTime));
        assertEquals(1, bookings.size());
    }

    @Test
    void isVenueAvailable_nullOrBlankArguments_failPreconditions() {
        BookingList bookings = new BookingList();
        for (String venue : new String[]{null, "", " \t "}) {
            assertThrows(AssertionError.class,
                    () -> bookings.isVenueAvailable(venue, first.getStartTime()));
        }
        assertThrows(AssertionError.class, () -> bookings.isVenueAvailable("LT1", null));
    }

    @Test
    void findByDate_sortsCopyAndSkipsLegacyAndOtherDates() {
        BookingList bookings = new BookingList();
        bookings.add(first);
        bookings.add(new Booking("exam", "Legacy", "LT1", "10AM"));
        bookings.add(new Booking("exam", "Next day", "LT1", "06/10/2026 09:00"));
        bookings.add(second);

        var matches = bookings.findByDate(LocalDate.of(2026, 10, 5));

        assertEquals(List.of(second, first), matches);
        matches.clear();
        assertEquals(4, bookings.size());
        assertSame(first, bookings.get(0));
        assertTrue(bookings.findByDate(LocalDate.of(2026, 10, 7)).isEmpty());
    }
}
