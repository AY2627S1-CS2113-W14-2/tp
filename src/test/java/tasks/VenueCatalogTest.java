package tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Checks preset venue lookup and independent availability results. */
class VenueCatalogTest {
    private static final LocalDateTime REQUESTED_TIME = LocalDateTime.of(2026, 10, 28, 14, 0);

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
