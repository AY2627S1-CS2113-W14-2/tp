package tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks preset venue lookup and independent availability results.
 *
 * @author Kytersaurus
 */
class VenueCatalogTest {
    private static final LocalDateTime REQUESTED_TIME = LocalDateTime.of(2026, 10, 28, 14, 0);

    @Test
    void findAvailableSlots_emptyList_returnsAllHoursInIndependentResults() {
        BookingList bookings = new BookingList();
        LocalDate date = REQUESTED_TIME.toLocalDate();
        List<LocalDateTime> expected = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            expected.add(date.atTime(hour, 0));
        }
        List<LocalDateTime> actual = VenueCatalog.findAvailableSlots(bookings, "LT19", date);
        assertEquals(expected, actual);
        actual.clear();
        assertEquals(expected, VenueCatalog.findAvailableSlots(bookings, "LT19", date));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void findAvailableSlots_halfHourBooking_blocksBothOverlappingSlotsOnly() {
        BookingList bookings = new BookingList();
        Booking existing = new Booking("exam", "Existing", "lt19", "28/10/2026 14:30");
        bookings.add(existing);
        List<LocalDateTime> slots = VenueCatalog.findAvailableSlots(
                bookings, " LT19 ", REQUESTED_TIME.toLocalDate());
        assertEquals(22, slots.size());
        assertFalse(slots.contains(REQUESTED_TIME));
        assertFalse(slots.contains(REQUESTED_TIME.plusHours(1)));
        assertTrue(slots.contains(REQUESTED_TIME.minusHours(1)));
        assertTrue(slots.contains(REQUESTED_TIME.plusHours(2)));
        assertEquals(1, bookings.size());
        assertSame(existing, bookings.get(0));
    }

    @Test
    void findAvailableSlots_alignedBooking_blocksOnlyItsHourAndAllowsAdjacentSlots() {
        BookingList bookings = new BookingList();
        Booking existing = new Booking("exam", "Existing", "LT19", "28/10/2026 14:00");
        bookings.add(existing);

        List<LocalDateTime> slots = VenueCatalog.findAvailableSlots(
                bookings, "lt19", REQUESTED_TIME.toLocalDate());

        assertEquals(23, slots.size());
        assertFalse(slots.contains(REQUESTED_TIME));
        assertTrue(slots.contains(REQUESTED_TIME.minusHours(1)));
        assertTrue(slots.contains(REQUESTED_TIME.plusHours(1)));
        assertEquals(1, bookings.size());
        assertSame(existing, bookings.get(0));
    }

    @Test
    void findAvailableSlots_bookingsTouchingDayBoundaries_doNotBlockTheDay() {
        BookingList bookings = new BookingList();
        Booking previousDay = new Booking("exam", "Previous day", "LT19", "31/12/2026 23:00");
        Booking nextDay = new Booking("exam", "Next day", "LT19", "02/01/2027 00:00");
        bookings.add(previousDay);
        bookings.add(nextDay);
        bookings.add(new Booking("lecture", "Other venue", "LT17", "01/01/2027 14:00"));

        List<LocalDateTime> slots = VenueCatalog.findAvailableSlots(
                bookings, "LT19", LocalDate.of(2027, 1, 1));

        assertEquals(24, slots.size());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), slots.getFirst());
        assertEquals(LocalDateTime.of(2027, 1, 1, 23, 0), slots.getLast());
        assertEquals(3, bookings.size());
        assertSame(previousDay, bookings.get(0));
        assertSame(nextDay, bookings.get(1));
    }

    @Test
    void findAvailableSlots_searchingDifferentVenues_returnsIndependentFilteredResults() {
        BookingList bookings = new BookingList();
        Booking first = new Booking("exam", "First venue", "LT19", "28/10/2026 14:30");
        Booking second = new Booking("lecture", "Second venue", "LT17", "28/10/2026 09:00");
        bookings.add(first);
        bookings.add(second);
        LocalDate date = REQUESTED_TIME.toLocalDate();

        List<LocalDateTime> firstSlots = VenueCatalog.findAvailableSlots(bookings, "LT19", date);
        List<LocalDateTime> secondSlots = VenueCatalog.findAvailableSlots(bookings, "LT17", date);

        assertEquals(22, firstSlots.size());
        assertEquals(23, secondSlots.size());
        assertTrue(firstSlots.contains(date.atTime(9, 0)));
        assertFalse(secondSlots.contains(date.atTime(9, 0)));
        assertTrue(secondSlots.contains(REQUESTED_TIME));
        secondSlots.clear();
        assertEquals(22, firstSlots.size());
        assertEquals(23, VenueCatalog.findAvailableSlots(bookings, "LT17", date).size());
        assertEquals(2, bookings.size());
        assertSame(first, bookings.get(0));
        assertSame(second, bookings.get(1));
    }

    @Test
    void findAvailableSlots_midnightOverlaps_blockFirstAndLastSlots() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Previous day", "LT19", "27/10/2026 23:30"));
        bookings.add(new Booking("exam", "Late booking", "LT19", "28/10/2026 23:30"));
        bookings.add(new Booking("exam", "Other venue", "LT17", "28/10/2026 14:00"));
        bookings.add(new Booking("exam", "Legacy", "LT19", "14:00"));
        List<LocalDateTime> slots = VenueCatalog.findAvailableSlots(
                bookings, "LT19", REQUESTED_TIME.toLocalDate());
        assertEquals(22, slots.size());
        assertEquals(REQUESTED_TIME.toLocalDate().atTime(1, 0), slots.getFirst());
        assertEquals(REQUESTED_TIME.toLocalDate().atTime(22, 0), slots.getLast());
        assertTrue(slots.contains(REQUESTED_TIME));
        assertEquals(4, bookings.size());
    }

    @Test
    void findAvailableSlots_invalidDependencies_failPreconditions() {
        BookingList bookings = new BookingList();
        LocalDate date = REQUESTED_TIME.toLocalDate();
        assertThrows(AssertionError.class, () -> VenueCatalog.findAvailableSlots(null, "LT19", date));
        assertThrows(AssertionError.class, () -> VenueCatalog.findAvailableSlots(bookings, "LT19", null));
        for (String venue : new String[]{null, "", "  ", "UNKNOWN"}) {
            assertThrows(AssertionError.class,
                    () -> VenueCatalog.findAvailableSlots(bookings, venue, date));
        }
    }

    @Test
    void findVenueInCatalog_ignoresCaseAndSurroundingWhitespace() {
        assertTrue(VenueCatalog.findVenueInCatalog("LT19"));
        assertTrue(VenueCatalog.findVenueInCatalog("  lt19\t"));
        assertTrue(VenueCatalog.findVenueInCatalog("com1-0201"));
        assertFalse(VenueCatalog.findVenueInCatalog("UNKNOWN-VENUE"));
        assertFalse(VenueCatalog.findVenueInCatalog("   "));
    }

    @Test
    void findAvailableVenues_emptyBookings_returnsUniqueIndependentPresetList() {
        BookingList bookings = new BookingList();
        List<String> available = VenueCatalog.findAvailableVenues(bookings, REQUESTED_TIME);
        List<String> original = new ArrayList<>(available);

        assertTrue(available.containsAll(List.of("COM1-0201", "COM3-01-20", "COM4SR31", "LT19")));
        assertEquals(available.size(), new HashSet<>(available).size());
        available.clear();
        assertEquals(original, VenueCatalog.findAvailableVenues(bookings, REQUESTED_TIME));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void findAvailableVenues_excludesOverlappingVenuesAndSkipsOtherDatesAndLegacyTiming() {
        BookingList bookings = new BookingList();
        Booking occupied = new Booking("exam", "Occupied", "lt19", "28/10/2026 14:00");
        bookings.add(occupied);
        bookings.add(new Booking("lecture", "Other time", "LT17", "28/10/2026 14:30"));
        bookings.add(new Booking("exam", "Other day", "LT15", "29/10/2026 14:00"));
        bookings.add(new Booking("exam", "Legacy", "LT14", "14:00"));

        List<String> available = VenueCatalog.findAvailableVenues(bookings, REQUESTED_TIME);

        assertFalse(available.contains("LT19"));
        assertFalse(available.contains("LT17"));
        assertTrue(available.containsAll(List.of("LT15", "LT14")));
        assertEquals(4, bookings.size());
        assertSame(occupied, bookings.get(0));
    }

    @Test
    void catalogueMethods_nullArguments_failPreconditions() {
        assertThrows(AssertionError.class, () -> VenueCatalog.findVenueInCatalog(null));
        assertThrows(AssertionError.class, () -> VenueCatalog.findAvailableVenues(null, REQUESTED_TIME));
        assertThrows(AssertionError.class,
                () -> VenueCatalog.findAvailableVenues(new BookingList(), null));
    }
}
