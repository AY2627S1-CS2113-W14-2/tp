package tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

/** Checks booking values, strict date parsing, and constructor preconditions. */
class BookingTest {
    @Test
    void constructor_validBooking_exposesDetailsAndDisplayText() {
        Booking booking = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");

        assertEquals("exam", booking.getDescription());
        assertEquals("exam", booking.getReason());
        assertEquals("Midterm", booking.getName());
        assertEquals("LT1", booking.getVenue());
        assertEquals("05/10/2026 14:00", booking.getTiming());
        assertEquals(LocalDateTime.of(2026, 10, 5, 14, 0), booking.getStartTime());
        assertEquals("B", booking.getTaskIcon());
        assertEquals("[B] exam - Midterm (venue: LT1, timing: 05/10/2026 14:00)", booking.toString());
    }

    @Test
    void constructor_legacyTiming_remainsViewableWithoutParsedTime() {
        Booking booking = new Booking("lecture", "Lecture", "LT2", "10AM-12PM");

        assertNull(booking.getStartTime());
        assertEquals("10AM-12PM", booking.getTiming());
        assertEquals("[B] lecture - Lecture (venue: LT2, timing: 10AM-12PM)", booking.toString());
    }

    @Test
    void constructor_invalidReason_failsPrecondition() {
        for (String reason : new String[]{null, "", "meeting", "EXAM"}) {
            assertThrows(AssertionError.class, () -> new Booking(reason, "Midterm", "LT1", "14:00"));
        }
    }

    @Test
    void constructor_nullOrBlankFields_failPreconditions() {
        for (String field : new String[]{null, "", " \t "}) {
            assertThrows(AssertionError.class, () -> new Booking("exam", field, "LT1", "14:00"));
            assertThrows(AssertionError.class, () -> new Booking("exam", "Midterm", field, "14:00"));
            assertThrows(AssertionError.class, () -> new Booking("exam", "Midterm", "LT1", field));
        }
    }

    @Test
    void parseStartTime_impossibleDate_isRejectedInsteadOfAdjusted() {
        assertThrows(DateTimeParseException.class, () -> Booking.parseStartTime("31/04/2026 14:00"));
        assertEquals(LocalDateTime.of(2028, 2, 29, 23, 59), Booking.parseStartTime("29/02/2028 23:59"));
    }

    @Test
    void getEndTime_isOneHourAfterStart_orNullForLegacyTiming() {
        Booking booking = new Booking("exam", "Midterm", "LT1", "05/10/2026 23:30");
        Booking legacy = new Booking("exam", "Legacy", "LT1", "10AM-12PM");

        assertEquals(LocalDateTime.of(2026, 10, 6, 0, 30), booking.getEndTime());
        assertEquals(Booking.DEFAULT_DURATION, java.time.Duration.ofHours(1));
        assertNull(legacy.getEndTime());
    }

    @Test
    void overlaps_sameVenueOnlyWhenOneHourSlotsIntersect() {
        Booking booking = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");
        LocalDateTime start = booking.getStartTime();

        assertTrue(booking.overlaps(" lt1 ", start));
        assertTrue(booking.overlaps("LT1", start.plusMinutes(59)));
        assertTrue(booking.overlaps("LT1", start.minusMinutes(59)));
        assertFalse(booking.overlaps("LT1", start.plusMinutes(60)));
        assertFalse(booking.overlaps("LT1", start.minusMinutes(60)));
        assertFalse(booking.overlaps("LT2", start));
        assertFalse(new Booking("exam", "Legacy", "LT1", "14:00").overlaps("LT1", start));
        assertThrows(AssertionError.class, () -> booking.overlaps(" ", start));
        assertThrows(AssertionError.class, () -> booking.overlaps("LT1", null));
    }
}
