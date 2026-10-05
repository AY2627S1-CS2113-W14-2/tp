package Ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import Exceptions.InvalidCommandException;
import Tasks.Booking;

/**
 * Checks booking reasons, required fields, and preservation of user-supplied details.
 */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parseBooking_exam_createsBooking() {
        Booking booking = parser.parseBooking("book r/exam v/LT1 t/10:30am");

        assertEquals("exam", booking.getReason());
        assertEquals("LT1", booking.getVenue());
        assertEquals("10:30am", booking.getTiming());
    }

    @Test
    void parseBooking_lecture_preservesSpacesAndDates() {
        Booking booking = parser.parseBooking("book r/lecture v/Lecture Theatre 2 t/05/10/2026 2:00pm");

        assertEquals("lecture", booking.getReason());
        assertEquals("Lecture Theatre 2", booking.getVenue());
        assertEquals("05/10/2026 2:00pm", booking.getTiming());
    }

    @Test
    void parseBooking_mixedCaseAndWhitespace_normalizesReason() {
        Booking booking = parser.parseBooking("  BOOK\tr/  LeCtUrE   v/ LT2   t/ 2:00pm  ");

        assertEquals("lecture", booking.getReason());
        assertEquals("LT2", booking.getVenue());
        assertEquals("2:00pm", booking.getTiming());
    }

    @Test
    void parseBooking_unsupportedReasons_throws() {
        for (String reason : new String[]{"meeting", "examination", "lectures", "exam lecture", ""}) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseBooking("book r/" + reason + " v/LT1 t/10:30am"), reason);
        }
    }

    @Test
    void parseBooking_missingOrBlankFields_throws() {
        String[] commands = {
            "book", "book   ", "book v/LT1 t/10:30am", "book r/exam t/10:30am",
            "book r/exam v/LT1", "book r/exam v/ t/10:30am", "book r/lecture v/LT1 t/   "
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_malformedFields_throws() {
        String[] commands = {
            "book exam LT1 10:30am", "book v/LT1 r/exam t/10:30am",
            "book r/exam v/LT1 t/10:30am r/lecture", "book r/exam v/LT1 v/LT2 t/10:30am",
            "book r/exam v/LT1 t/10:30am t/11:30am", "book r/exam v/LT1 t/10:30am x/extra"
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_nonBookingCommand_throws() {
        assertThrows(InvalidCommandException.class, () -> parser.parseBooking("bye"));
        assertThrows(InvalidCommandException.class, () -> parser.parseBooking("todo r/exam v/LT1 t/10:30am"));
    }
}
