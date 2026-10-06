package ui;

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
        Booking booking = parser.parseBooking("book r/exam n/CS2113 Midterm v/LT1 t/10:30am");

        assertEquals("exam", booking.getReason());
        assertEquals("CS2113 Midterm", booking.getName());
        assertEquals("LT1", booking.getVenue());
        assertEquals("10:30am", booking.getTiming());
    }

    @Test
    void parseBooking_lecture_preservesSpacesAndDates() {
        Booking booking = parser.parseBooking(
                "book r/lecture n/Part a/b Lecture v/Lecture Theatre 2 t/05/10/2026 2:00pm");

        assertEquals("lecture", booking.getReason());
        assertEquals("Part a/b Lecture", booking.getName());
        assertEquals("Lecture Theatre 2", booking.getVenue());
        assertEquals("05/10/2026 2:00pm", booking.getTiming());
    }

    @Test
    void parseBooking_mixedCaseAndWhitespace_normalizesReason() {
        Booking booking = parser.parseBooking("  BOOK\tr/  LeCtUrE\tn/  Week 1 Lecture   v/ LT2   t/ 2:00pm  ");

        assertEquals("lecture", booking.getReason());
        assertEquals("Week 1 Lecture", booking.getName());
        assertEquals("LT2", booking.getVenue());
        assertEquals("2:00pm", booking.getTiming());
    }

    @Test
    void parseBooking_unsupportedReasons_throws() {
        for (String reason : new String[]{"meeting", "examination", "lectures", "exam lecture", ""}) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseBooking("book r/" + reason + " n/Midterm v/LT1 t/10:30am"), reason);
        }
    }

    @Test
    void parseBooking_missingOrBlankFields_throws() {
        String[] commands = {
            "book", "book   ", "book n/Midterm v/LT1 t/10:30am", "book r/exam v/LT1 t/10:30am",
            "book r/exam n/Midterm t/10:30am", "book r/exam n/Midterm v/LT1",
            "book r/ n/Midterm v/LT1 t/10:30am", "book r/exam n/ v/LT1 t/10:30am",
            "book r/exam n/Midterm v/ t/10:30am", "book r/lecture n/Week 1 v/LT1 t/   ",
            "book r/exam v/LT1 t/10:30am n/"
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_malformedFields_throws() {
        String[] commands = {
            "book exam Midterm LT1 10:30am", "book unexpected r/exam n/Midterm v/LT1 t/10:30am",
            "book R/exam n/Midterm v/LT1 t/10:30am", "book x/extra r/exam n/Midterm v/LT1 t/10:30am",
            "book r/examn/Midterm v/LT1 t/10:30am", "book r/exam n/Midterm venue/LT1 t/10:30am",
            "book n/Midterm r/exam v/LT1 t/10:30am", "book r/exam n/Midterm t/10:30am v/LT1",
            "book r/exam n/Midterm n/Final v/LT1 t/10:30am"
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_nonBookingCommand_throws() {
        assertThrows(InvalidCommandException.class, () -> parser.parseBooking("bye"));
        assertThrows(InvalidCommandException.class,
                () -> parser.parseBooking("todo r/exam n/Midterm v/LT1 t/10:30am"));
    }

    @Test
    void parseCancel_validName_returnsName() {
        assertEquals("CS2113 Midterm", parser.parseCancel("cancel n/CS2113 Midterm"));
        assertEquals("Part a/b", parser.parseCancel("  CANCEL\tn/  Part a/b  "));
    }

    @Test
    void parseCancel_missingOrBlankName_throws() {
        String[] commands = {"cancel", "cancel   ", "cancel n/", "cancel n/   ", "cancel Midterm", "cancel r/exam"};
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseCancel(command), command);
        }
    }

    @Test
    void parseViewList_noFilter_returnsBookings() {
        assertEquals("bookings", parser.parseViewList("list"));
        assertEquals("bookings", parser.parseViewList("  list   "));
    }

    @Test
    void parseViewList_validFilters_returnsRequestedType() {
        assertEquals("exams", parser.parseViewList("list exams"));
        assertEquals("lectures", parser.parseViewList("list lectures"));
        assertEquals("lectures", parser.parseViewList("  LIST\tlectures  "));
    }

    @Test
    void parseViewList_invalidInputs_throws() {
        String[] commands = {
            "list meetings",
            "list exam",
            "list lecture",
            "list exams lectures",
            "list exams extra",
            "book",
            "bye"
        };

        for (String command : commands) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseViewList(command), command);
        }
    }
}
